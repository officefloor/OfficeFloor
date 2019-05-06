import { Component, OnInit, AfterViewInit } from '@angular/core';

// Loaded via PayPal script
declare let paypal: any;

@Component( {
    selector: 'app-checkout',
    templateUrl: './checkout.component.html',
    styleUrls: ['./checkout.component.css']
} )
export class CheckoutComponent implements OnInit, AfterViewInit {

    constructor() { }

    ngOnInit() {
    }

    private loadExternalScript( scriptUrl: string ) {
        return new Promise(( resolve, reject ) => {
            const scriptElement = document.createElement( 'script' )
            scriptElement.src = scriptUrl
            scriptElement.onload = resolve
            document.body.appendChild( scriptElement )
        } )
    }

    ngAfterViewInit(): void {

        // TODO load configuration
        const CLIENT_ID = 'AZVitvHU3nWyNt8rTdndNq8MP_CDd-xShU6iO1kMPYrN8ZfGj0d9hAk29MrXZD0WpaAFPO0B1DP4rvLL'
        const ENVIRONMENT = 'sandbox'
        const CURRENCY = 'AUD'

        // Load Paypal
        this.loadExternalScript( `https://www.paypal.com/sdk/js?client-id=${CLIENT_ID}&currency=${CURRENCY}` ).then(() => {
            paypal.Buttons( {
                createOrder: function( data, actions ) {
                    // Set up the transaction
                    return actions.order.create( {
                        purchase_units: [{
                            amount: {
                                value: '5.00', currency: CURRENCY
                            }
                        }]
                    } );
                },
                onApprove: function( data, actions ) {

                    // TODO call server with orderId
                    console.log( 'Order: ' + data.orderID + " with data:\n\n" + JSON.stringify( data, null, 2 ) + "\n\n" );

                    // Capture the funds from the transaction
                    return actions.order.capture().then( function( details ) {
                        // Show a success message to your buyer
                        console.log( 'Transaction details: ' + JSON.stringify( details, null, 2 ) )
                    } );
                }
            } ).render( '#paypal-button' );
        } );
    }

}
