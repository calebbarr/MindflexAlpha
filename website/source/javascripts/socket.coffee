socket = io.connect('localhost:8081')
socket.on 'brainwaves', (data) ->
  brainWaves = $.parseJSON(data)
  brainWavesQueue.push brainWaves