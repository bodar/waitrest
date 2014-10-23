<!DOCTYPE html>
<html>
    <head>
        <title>Counts</title>
    </head>
    <body>
    In-memory requests/responses: <b>$inMemoryCount$</b>
    <hr/>
    Requests/responses imported from files: <b>$totalImportedOrderCount$</b><br/><br/>
    $if(importedOrderCounts)$
    <table>
        <thead>
            <tr>
                <th>File Path</th>
                <th>Count</th>
            </tr>
        </thead>
        <tbody>
         $importedOrderCounts: { orderCount |
              <tr>
                  <td>$orderCount.filePath$</td>
                  <td>$orderCount.count$</td>
              </tr>
        }$
        </tbody>
    </table>
    $endif$
    </body>
</html>