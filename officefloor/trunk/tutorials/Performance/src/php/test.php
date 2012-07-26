<?php

$val = substr( $_SERVER[REQUEST_URI], 12, 1 );
if ( $val == "N" ) {
    // News feed
    echo 'n';
    
} else {
    // Simulate database connect, interaction, close
    usleep(500);
    usleep(1000);
    usleep(500); 
    echo 'd';
}
?>