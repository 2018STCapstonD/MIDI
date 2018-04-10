class Executer:
    def __init__(self, tcpServer):
        self.andRaspTCP = tcpServer

    def startCommand(self, command):
        if command == "123\n":
            self.andRaspTCP.sendAll("321\n")