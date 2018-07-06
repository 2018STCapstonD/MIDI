import socket

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

sock.connect(('192.168.0.5',37771))

sock.send("hi".encode())

data = sock.recv(1024)
