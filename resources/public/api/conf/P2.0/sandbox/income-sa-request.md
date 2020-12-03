<table>
    <col width="25%">
    <col width="35%">
    <col width="40%">
    <thead>
    <tr>
        <th>Scenario</th>
        <th>Parameters</th>
        <th>Response</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><p>Happy path</p></td>
        <td><p>matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430</p><p>fromTaxYear=2013-14</p><p>toTaxYear=2015-16</p></td>
        <td><p>200 (OK)</p><p>Payload as response example above</p></td>
    </tr>
    <tr>
        <td>Missing matchId</td>
        <td>matchId query parameter missing</td>
        <td><p>400 (Bad Request)</p>
        <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;matchId is required&quot; }</p>
        </td>
    </tr>
    <tr>
        <td>Missing fromTaxYear</td>
        <td>fromTaxYear query parameter missing</td>
        <td><p>400 (Bad Request)</p>
        <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;fromTaxYear is required&quot; }</p>
        </td>
    </tr>
    <tr>
         <td><p>toTaxYear earlier than fromTaxYear</p></td>
         <td><p>e.g. fromTaxYear=2016-17 toTaxYear=2015-16</p></td>
         <td><p>400 (Bad Request)</p>
         <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Invalid time period requested&quot; }</p></td>
    </tr>
    <tr>
         <td><p>toTaxYear later than the current tax year</p></td>
         <td><p>e.g. fromTaxYear=2016-17 toTaxYear=2098-99</p></td>
         <td><p>400 (Bad Request)</p>
         <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;toTaxYear is later than the current tax year&quot; }</p></td>
    </tr>
    <tr>
         <td>From tax year requested is earlier than current tax year minus 6</td>
         <td><p>e.g. fromTaxYear=2008-09</p>
         </td>
         <td>
           <p>400 (Bad Request)</p>
           <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;fromTaxYear earlier than maximum allowed&quot; }</p>
         </td>
    </tr>
    <tr>
         <td><p>Invalid tax year format</p></td>
         <td><p>Any tax year that is not in the format YYYY-YY</p>
         <p>e.g. 2017-2018</p></td>
         <td><p>400 (Bad Request)</p>
         <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;fromTaxYear: invalid tax year format&quot; }</p>
         <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;toTaxYear: invalid tax year format&quot; }</p></td>
    </tr>
    <tr>
        <td><p>Incorrect matchId</p></td>
        <td><p>The matchId is not valid</p></td>
        <td><p>404 (Not Found)</p>
        <p>{ &quot;code&quot; : &quot;NOT_FOUND&quot;,<br/>&quot;message&quot; : &quot;The resource cannot be found&quot; }</p></td>
    </tr>
    </tbody>
</table>