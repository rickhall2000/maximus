# maximus

Several random files to get familiar with Datomic.  

This is not actively maintained. I just wanted to keep a record of what I did.

## Usage

client.clj has a function active-count. It is meant to be a peer that selects data that was inserted by any other peer.  

demo.clj does some simple creates, reads and updates, and displays the results.  

handler.clj defines some routes that can be called to return dummy data in xml or edn formats, and also some live data buit by reportdata.clj.  

stress.clj does a lot of creates and updates. The first def is the database to be used. It can be either datomic or postgres.  

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
