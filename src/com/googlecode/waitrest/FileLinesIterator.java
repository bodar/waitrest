package com.googlecode.waitrest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.googlecode.waitrest.Line.line;

public class FileLinesIterator implements Iterator<Line> {

    private static final char LINE_FEED = '\n';
    private static final char CARRIAGE_RETURN = '\r';
    private static final int BUFFER_LENGTH = 8192;

    private final FileInputStream fileInputStream;

    private ByteBuffer buffer;
    private long filePosition;
    private int positionInBuffer;
    private int lineLength;
    private StringBuilder stringBuilder;

    public FileLinesIterator(File input) throws IOException {
        this.buffer = ByteBuffer.allocate(BUFFER_LENGTH);
        fileInputStream = new FileInputStream(input);
        final FileChannel channel = fileInputStream.getChannel();
        channel.read(buffer, filePosition);
        this.buffer.flip();
        this.filePosition = 0;
        this.positionInBuffer = 0;
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public boolean hasNext() {
        refillBufferIfNeeded();
        if (buffer.limit() == 0) {
            closeFileStream();
            return false;
        }
        return true;
    }

    @Override
    public Line next() {
        if (buffer.limit() == 0) {
            closeFileStream();
            throw new NoSuchElementException();
        }

        lineLength = 0;
        stringBuilder.setLength(0);

        while (true) {
            if (!refillBufferIfNeeded()) break;
            final char nextChar = (char) buffer.get(positionInBuffer);
            if (!isLineDelimiter(nextChar)) {
                stringBuilder.append(nextChar);
            } else if (isCarriageReturnFollowedByLineFeed(nextChar)) {
                incrementCounters();
            }
            incrementCounters();
            if (isLineDelimiter(nextChar)) break;
        }
        if (lineLength == 0) {
            closeFileStream();
            throw new NoSuchElementException();
        }
        return line(stringBuilder.toString(), lineLength);
    }

    @Override
    public void remove() {

    }

    private void incrementCounters() {
        lineLength++;
        positionInBuffer++;
        filePosition++;
    }

    private boolean isCarriageReturnFollowedByLineFeed(char nextChar) {
        if (nextChar == CARRIAGE_RETURN && positionInBuffer + 1 < buffer.limit()) {
            final int followingCharacter = (char) buffer.get(positionInBuffer + 1);
            return followingCharacter == LINE_FEED;
        }
        return false;
    }

    private boolean refillBufferIfNeeded() {
        if (positionInBuffer < buffer.limit()) return true;
        try {
            final FileChannel fileChannel = fileInputStream.getChannel();
            final int bytesRead = fileChannel.read(buffer, filePosition);
            if (bytesRead == -1) {
                buffer.limit(0);
            } else {
                buffer.limit(bytesRead);
                buffer.rewind();
                positionInBuffer = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.hasRemaining();
    }

    private boolean isLineDelimiter(char aChar) {
        return aChar == LINE_FEED || aChar == CARRIAGE_RETURN;
    }

    private void closeFileStream() {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}