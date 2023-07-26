import zmq
import time

context = zmq.Context()

#  Socket to talk to server
print("Connecting to ergo node")
socket = context.socket(zmq.SUB)
socket.connect("tcp://127.0.0.1:9060")

# Subscribe to all topics
socket.subscribe("")

#  Do 10 requests, waiting each time for a response
while True:

    #  Get the reply.
    message = socket.recv().decode()
    if message.startswith("utx"):
        print(f"{time.asctime()} - Unconfirmed tx hit the mempool: {message[3:]}")
    else:
        print(f"{time.asctime()} - New block mined with header: {message[3:67]}")
    