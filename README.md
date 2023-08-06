# ergo-node-zmqpub
Picks up events from an ergo node and publishes them on a zeromq pub socket.

| config key     | Description                                     | Default |
|----------------|-------------------------------------------------| --- |
| nodeURL   | url of the ergo node                            | http://213.239.193.208:9053 |
| nodePeersPort | P2P port of the ergo node                       | 9030 |
| zmqIP         | IP the zeromq pub/sub socket will be created on | 0.0.0.0 |
| zmqPort       | Port the zeromq pub/sub will be create on       | 9060 |

Note the Ergonnection library used requires JDK 17+

To run on linux:
```bash
./run.sh
```
To run on windows:
```
run.bat
```

To run using docker
```
docker compose up -d
```

