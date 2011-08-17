<html>
    <head>
        <title>Order Taken</title>
    </head>
    <body>
        <form action="$url$" method="POST">
            $formParameters: { param |
                $param$ <input type="text" name="$param$"" />
            }$
            <input type="submit" value="Submit" />
        </form>
    </body>
</html>