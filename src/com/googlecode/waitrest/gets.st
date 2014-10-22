<html>
    <head>
        <title>GET orders</title>
    </head>
    <body>
    $if(urls)$
        <ul>

            $urls: { url |
                <li>
                    <a href='$url$'>$url$</a>
                </li>
            }$
        </ul>
     $else$
        <p>
            No orders found.
        </p>
     $endif$
    </body>
</html>