# SPCR 

as in Spend Parse Categorize Report is a simple website that allows to upload, parse CSV files and store them in MongoDb, aimed at a help in categorizing expenses via bank statement processing.

## Installation

For dev installation , download MongoDb from http://www.mongodb.org/downloads

Run mongod process if needed, i.e. if you just downloaded Mongo binaries run command below 

    $ ./mongo-download-folder/bin/mongod --dbpath ~/path-where-you-want-to-store-mongo-data --smallfiles
    
Checkout this repo from github

Run SPCR from spcr root folder (7777 is a sample port )

    $ lein run 7777 dev
    

## Usage

Test on: http://spcr.herokuapp.com


### Might be Useful

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
