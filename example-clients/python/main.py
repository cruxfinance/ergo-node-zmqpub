import zmq
import time

context = zmq.Context()

print("Connecting to ergo node")
socket = context.socket(zmq.SUB)
socket.connect("tcp://127.0.0.1:9060")
# Subscribe to the topics you are interested in
socket.subscribe("mempool")
socket.subscribe("newBlock")

while True:
    # First receive the topic
    topic = socket.recv().decode()
    # Then receive the message content
    message = socket.recv().decode()
    if topic == "newBlock":
        print(f"{time.asctime()} - New block mined with header {message[:64]}")
    elif topic == "mempool":
        print(f"{time.asctime()} - Unconfirmed tx hit the mempool: {message}")