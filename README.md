# ergo-node-zmqpub
Picks up events from an ergo node and publishes them on a zeromq pub socket.

The .env.example file has relevant env variables:

| Env var name | Description | Default |
| --- | --- | --- |
| ERGO_NODE_IP | IP of the ergo node | 127.0.0.1 |
| ERGO_NODE_PORT | P2P port of the ergo node | 9030 |
| ZMQ_IP | IP the zeromq pub/sub socket will be created on | 0.0.0.0 |
| ZMQ_PORT | Port the zeromq pub/sub will be create on | 9060 |

Note the Ergonnection library used requires JDK 17+
