from random import randint as r
import socket

from twisted.internet import reactor
from twisted.internet import task


clientsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
clientsocket.connect(('localhost', 9999))
def sendFakeBrainwaves():
    clientsocket.send(",".join(
    [str(x) for x in \
        [0] +\
        [r(0,100) for x in range(2)] +\
        [r(0,100000) for x in range(8)]
    ])+"\n")

task.LoopingCall(sendFakeBrainwaves).start(1.0)
reactor.run()