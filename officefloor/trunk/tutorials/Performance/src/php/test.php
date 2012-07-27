<?php

$val = substr( $_SERVER[REQUEST_URI], 12, 1 );
if ( $val == "N" ) {
    // News feed
    echo 'n';
    
} else {
    // Simulate database connect, interaction, close
    usleep(5000);
    usleep(10000);
    usleep(5000); 
    echo 'd';
}
?>