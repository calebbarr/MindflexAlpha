from random import randint as r
import socket

from twisted.internet import reactor
from twisted.internet import task


def sendFakeBrainwaves():
    brainwaves = ",".join(
    [str(x) for x in \
        [0] +\
        [r(0,100) for x in range(2)] +\
        [r(0,100000) for x in range(8)]
    ])+"\n"
    print brainwaves
    clientsocket.send(brainwaves)

serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.bind(('localhost', 9990))
serversocket.listen(1)
clientsocket, address = serversocket.accept()
task.LoopingCall(sendFakeBrainwaves).start(1.0)
reactor.run()