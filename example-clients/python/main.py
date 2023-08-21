import zmq
import time

context = zmq.Context()

print("Connecting to ergo node")
socket = context.socket(zmq.SUB)
socket.connect("tcp://127.0.0.1:9060")
# Subscribe to the topics you are interested in
socket.subscribe("mempool")
socket.subscribe("newBlock")
socket.subscribe("newHeight")

while True:
    # First receive the topic
    topic = socket.recv().decode()
    
    if topic == "newBlock":
        header_id = socket.recv().decode()
        print(f"{time.asctime()} - New block mined with header {header_id}")
    if topic == "newHeight":
        header_id = socket.recv().decode()
        height = socket.recv().decode()
        print(f"{time.asctime()} - New block mined with header {header_id} and height {height}")
    elif topic == "mempool":
        message = socket.recv().decode()
        print(f"{time.asctime()} - Unconfirmed tx hit the mempool: {message}")