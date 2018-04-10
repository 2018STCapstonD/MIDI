import tcpServer
import executer
import queue
import time

# make public queue
commandQueue = queue.Queue()

# init module
andRaspTCP = tcpServer.TCPServer(commandQueue, '192.168.0.5', 37771)
andRaspTCP.start()

# set module to executer
commandExecuter = executer.Executer(andRaspTCP)

while True:
    try:
        command = commandQueue.get()
        commandExecuter.startCommand(command)
    except Exception as e:
        print(e)

# while True:
#    time.sleep(3)
#    andRaspTCP.sendAll("321\n")]