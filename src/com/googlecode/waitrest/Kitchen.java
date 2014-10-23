package com.googlecode.waitrest;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.regex.Matches;
import com.googlecode.totallylazy.regex.Regex;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.io.HierarchicalPath;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;
import static com.googlecode.utterlyidle.HttpMessageParser.parseResponse;
import static com.googlecode.utterlyidle.RequestBuilder.modify;
import static com.googlecode.waitrest.Waitress.REQUEST_SEPARATOR;

public class Kitchen {

    private final Map<Request, Response> ordersInMemory = new ConcurrentHashMap<Request, Response>();
    private final Map<Integer, Set<LocationInFile>> ordersImportedFromFiles = new ConcurrentHashMap<Integer, Set<LocationInFile>>();
    private final CookBook cookBook;

    private Kitchen(CookBook cookBook) {
        this.cookBook = cookBook;
    }

    public static Kitchen kitchen(CookBook cookBook) {
        return new Kitchen(cookBook);
    }

    public static Mapper<String, Pair<Request, Response>> prepareOrder() {
        return new Mapper<String, Pair<Request, Response>>() {
            @Override
            public Pair<Request, Response> call(String requestAndResponse) throws Exception {
                Sequence<String> requestAndResponseSequence = sequence(requestAndResponse.split(Waitress.RESPONSE_SEPARATOR));
                Matches matches = Regex.regex("^[\r\n]{1,2}(.*)[\r\n]{3,6}$", Pattern.DOTALL).findMatches(requestAndResponseSequence.second());
                if (matches.isEmpty()) {
                    throw new IllegalArgumentException("Request response not in expected waitrest format: " + requestAndResponseSequence.second());
                }
                final Request request = parseRequest(requestAndResponseSequence.first().trim());
                final Response response = parseResponse(matches.head().group(1));
                return pair(request, response);
            }
        };
    }

    public Response receiveOrder(Request request, Response response) {
        return ordersInMemory.put(modify(request).uri(request.uri().dropScheme().dropAuthority()).build(), response);
    }

    public void takeIndexedOrder(Uri requestUri, LocationInFile locationInFile) {
        final int hashOfRequest = requestUri.dropScheme().dropAuthority().hashCode();
        Set<LocationInFile> locations = ordersImportedFromFiles.get(hashOfRequest);
        if (locations == null) {
            locations = new HashSet<LocationInFile>();
            ordersImportedFromFiles.put(hashOfRequest, locations);
        }
        locations.add(locationInFile);
    }

    public Option<Response> serve(Request request) {
        final int hashCodeOfRequest = hashCodeOfRequest(request);

        final Set<LocationInFile> locationInFiles = ordersImportedFromFiles.containsKey(hashCodeOfRequest) ? ordersImportedFromFiles.get(hashCodeOfRequest) : Collections.<LocationInFile>emptySet();
        final Sequence<Pair<Request, Response>> potentialMatches = sequence(locationInFiles).map(toRequestResponsePairs()).join(Maps.pairs(ordersInMemory));
        final Option<Request> matchedRequest = potentialMatches.map(Callables.<Request>first()).filter(where(Requests.path(), is(HierarchicalPath.hierarchicalPath(request.uri().path())))).
                filter(where(query(), is(cookBook.correctForQueryParameters(Requests.query(request))))).
                filter(where(Requests.method(), is(request.method()))).
                filter(where(header(CONTENT_TYPE), is(stripCharset(request.headers().getValue(CONTENT_TYPE))))).
                filter(where(entity(), is(cookBook.correctForContentType(request)))).
                headOption();
        return matchedRequest.map(new Mapper<Request, Response>() {
            @Override
            public Response call(Request request) throws Exception {
                return potentialMatches.find(where(Callables.first(Request.class), is(request))).get().second();
            }
        });
    }

    private Function1<LocationInFile, Pair<Request, Response>> toRequestResponsePairs() {
        return new Mapper<LocationInFile, String>() {
            @Override
            public String call(final LocationInFile locationInFile) throws Exception {
                return using(new RandomAccessFile(locationInFile.getFilePath(), "r"), new Callable1<RandomAccessFile, String>() {
                    @Override
                    public String call(RandomAccessFile inputFile) throws Exception {
                        inputFile.seek(locationInFile.getPosition());
                        final StringBuilder requestAndResponse = new StringBuilder();
                        String readLine;
                        while ((readLine = inputFile.readLine()) != null && !readLine.equals(REQUEST_SEPARATOR)) {
                            requestAndResponse.append(readLine + "\n");
                        }
                        return requestAndResponse.toString();
                    }
                });
            }
        }.then(prepareOrder());
    }

    public String stripCharset(String contentType) {
        return Strings.isEmpty(contentType) ? "" : Regex.regex("^([^\\s^;]+)[;\\s]*").extract(contentType).headOption().getOrElse("");
    }

    private Callable1<Request, String> header(final String header) {
        return new Callable1<Request, String>() {
            @Override
            public String call(Request request) throws Exception {
                return stripCharset(request.headers().getValue(header));
            }
        };
    }

    private Callable1<Request, Entity> entity() {
        return new Callable1<Request, Entity>() {
            @Override
            public Entity call(Request request) throws Exception {
                return request.entity();
            }
        };
    }

    private Callable1<Request, QueryParameters> query() {
        return new Callable1<Request, QueryParameters>() {
            @Override
            public QueryParameters call(Request request) throws Exception {
                return Requests.query(request);
            }
        };
    }

    public Map<Request, Response> allOrdersInMemory(String orderType) {
        return Maps.map(pairs(ordersInMemory).filter(orderType(orderType)));
    }

    public Map<Request, Response> allOrdersInMemory() {
        return ordersInMemory;
    }

    public void deleteAllOrders() {
        ordersInMemory.clear();
        ordersImportedFromFiles.clear();
    }

    public int takeOrdersFrom(final File file) throws Exception {
        long offsetInFile = 0;
        int count = 0;
        final FileLinesIterator iterator = new FileLinesIterator(file);
        while (iterator.hasNext()) {
            Line nextLine = iterator.next();
            offsetInFile += nextLine.getLength();
            if (nextLine.getContent().startsWith(REQUEST_SEPARATOR)) {
                nextLine = iterator.next();
                final String[] splitRequest = nextLine.getContent().split(" ");
                final Uri requestUri = Uri.uri(splitRequest[1]);
                final LocationInFile locationInFile = new LocationInFile(file.getPath(), offsetInFile);
                takeIndexedOrder(requestUri, locationInFile);
                count++;
                offsetInFile += nextLine.getLength();
            }
        }
        return count;
    }


    private Predicate<? super Pair<Request, Response>> orderType(final String orderType) {
        return new Predicate<Pair<Request, Response>>() {
            @Override
            public boolean matches(Pair<Request, Response> requestResponsePair) {
                return requestResponsePair.first().method().equals(orderType);
            }
        };
    }

    private int hashCodeOfRequest(Request request) {
        return request.uri().dropScheme().dropAuthority().toString().hashCode();
    }

    public Sequence<Pair<String, Integer>> importedOrderCounts() {
        final Collection<Set<LocationInFile>> values = ordersImportedFromFiles.values();
        return Sequences.flatMap(values, Functions.<Set<LocationInFile>>identity()).
                groupBy(LocationInFile.filePath()).
                mapConcurrently(new Mapper<Group<String, LocationInFile>, Pair<String, Integer>>() {
                    @Override
                    public Pair<String, Integer> call(Group<String, LocationInFile> locationsGroupedByPath) throws Exception {
                        return Pair.pair(locationsGroupedByPath.key(), locationsGroupedByPath.size());
                    }
                });
    }

}
