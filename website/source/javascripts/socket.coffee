socket = io.connect('localhost:8080')
socket.on 'brainwaves', (data) ->
  brainWaves = $.parseJSON(data)
  brainWavesQueue.push brainWaves