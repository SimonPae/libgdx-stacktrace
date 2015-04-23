<?php
        if ( $_POST['stacktrace'] == "" || $_POST['package_version'] == "" || $_POST['package_name'] == "" ) {
                die("This script is used to collect field test crash stacktraces. No personal information is transmitted, collected or stored.<br/>For more information, please contact <a href='your-receiver-mail-address@your.domain'>your-receiver-mail-address@your.domain</a>");
        }
        $random = rand(1000,9999);
        $version = $_POST['package_version'];
        $package = $_POST['package_name'];
        $time= $_POST['time'];

        //### write error file to disk
        //$handle = fopen($time."-".$package."-trace-".$version."-".time()."-".$random, "w+");
        //fwrite($handle, $_POST['stacktrace']);
        //fclose($handle);

        // Uncomment and change the following line to have exceptions mailed to you
        mail("your-receiver-mail-address@your.domain","IMPORTANT: Exception received (".$package." Version ".$version.")",$_POST['stacktrace'], "from:your-sender-mail-address-on-server@your.domain");
?>