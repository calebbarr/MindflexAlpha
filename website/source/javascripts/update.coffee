updateInterval = 30
totalPoints = 100
MIN_VALUE = -.5
MAX_VALUE = 2

dataAccordingToChoices = ->
  choiceContainer = $('#choices')
  data = []
  choiceContainer.find('input:checked').each ->
    key = $(this).attr('name')
    if key and datasets[key]
      data.push datasets[key]
      datasets[key].isChecked = true
    return
  choiceContainer.find('input:checkbox:not(:checked)').each ->
    key = $(this).attr('name')
    datasets[key].isChecked = false
    return
  data

setupDivs = ->
  choiceContainer = $('#choices')
  $.each datasets, (key, val) ->
    choiceContainer.append '<br/><input type=\'checkbox\' name=\'' + key + '\' checked=\'checked\' id=\'id' + key + '\'></input>' + '<label for=\'id' + key + '\'>' + val.label + '</label>'
    return
  $('#footer').prepend 'Flot ' + $.plot.version + ' &ndash; '
  return

REDRAW_THRESHOLD = .2
# var minValue = -.5
# var maxValue = .5
brainWavesQueue = []
datasets = 
  'attention':
    label: 'attention'
    color: 0
    isChecked: true
  'meditation':
    label: 'meditation'
    color: 1
    isChecked: true
  'delta':
    label: 'delta'
    color: 2
    isChecked: true
  'theta':
    label: 'theta'
    color: 3
    isChecked: true
  'low alpha':
    label: 'low alpha'
    color: 4
    isChecked: true
  'high alpha':
    label: 'high alpha'
    color: 5
    isChecked: true
  'low beta':
    label: 'low beta'
    color: 6
    isChecked: true
  'high beta':
    label: 'high beta'
    color: 7
    isChecked: true
  'low gamma':
    label: 'low gamma'
    color: 8
    isChecked: true
  'high gamma':
    label: 'high gamma'
    color: 9
    isChecked: true

Array.max = (array) ->
  Math.max.apply Math, array

Array.min = (array) ->
  Math.min.apply Math, array

$ ->

  newMinOrMax = ->
    redraw = false
    maxes = []
    mins = []
    if datasets['attention'].isChecked
      maxes.push Array.max(data[0])
      mins.push Array.min(data[0])
    if datasets['meditation'].isChecked
      maxes.push Array.max(data[1])
      mins.push Array.min(data[1])
    if datasets['delta'].isChecked
      maxes.push Array.max(data[2])
      mins.push Array.min(data[2])
    if datasets['theta'].isChecked
      maxes.push Array.max(data[3])
      mins.push Array.min(data[3])
    if datasets['low alpha'].isChecked
      maxes.push Array.max(data[4])
      mins.push Array.min(data[4])
    if datasets['high alpha'].isChecked
      maxes.push Array.max(data[5])
      mins.push Array.min(data[5])
    if datasets['low beta'].isChecked
      maxes.push Array.max(data[6])
      mins.push Array.min(data[6])
    if datasets['high beta'].isChecked
      maxes.push Array.max(data[7])
      mins.push Array.min(data[7])
    if datasets['low gamma'].isChecked
      maxes.push Array.max(data[8])
      mins.push Array.min(data[8])
    if datasets['high gamma'].isChecked
      maxes.push Array.max(data[9])
      mins.push Array.min(data[9])
    max = Array.max(maxes)
    min = Array.min(mins)
    if Math.abs(max - maxValue) >= REDRAW_THRESHOLD
      maxValue = max
      plot.getAxes().yaxis.max = maxValue
      redraw = true
    if Math.abs(min - minValue) >= REDRAW_THRESHOLD
      minValue = min
      plot.getAxes().yaxis.min = minValue
      redraw = true
    redraw

  getData = ->
    res = [
      []
      []
      []
      []
      []
      []
      []
      []
      []
      []
    ]
    if brainWavesQueue.length > 0
      # most of the time this will not happen, queue.length will be 0
      brainFrame = brainWavesQueue[0]
      brainWavesQueue = brainWavesQueue.slice(1)
      i = 0
      while i < data.length
        data[i] = data[i].slice(1)
        i++
      # and push the brainwave onto each one 
      data[0].push brainFrame.attention
      data[1].push brainFrame.meditation
      data[2].push brainFrame.delta
      data[3].push brainFrame.theta
      data[4].push brainFrame.lowAlpha
      data[5].push brainFrame.highAlpha
      data[6].push brainFrame.lowBeta
      data[7].push brainFrame.highBeta
      data[8].push brainFrame.lowGamma
      data[9].push brainFrame.highGamma
    else
      # if you have no new brainwaves, slice and push previous value (just to keep things *moving*, you know)
      i = 0
      while i < data.length
        if data[i].length > 0
          data[i] = data[i].slice(1)
        while data[i].length < totalPoints
          prev = if data[i].length > 0 then data[i][data[i].length - 1] else 1.0
          # push previous value, default to 1.0
          data[i].push prev
          # Zip the generated y values with the x values
          j = 0
        while j < data[i].length
          res[i].push [
            j
            data[i][j]
          ]
          j++
        i++
    while i < res.length
      datasets[getLabel(i)].data = res[i]
      i++
    return

  getLabel = (i) ->
    label = ''
    switch i
      when 0
        label = 'attention'
      when 1
        label = 'meditation'
      when 2
        label = 'delta'
      when 3
        label = 'theta'
      when 4
        label = 'low alpha'
      when 5
        label = 'high alpha'
      when 6
        label = 'low beta'
      when 7
        label = 'high beta'
      when 8
        label = 'low gamma'
      when 9
        label = 'high gamma'
    label

  update = ->
    getData()
    plot.setData dataAccordingToChoices()
    # if(newMinOrMax())
    #   plot.setupGrid();
    plot.draw()
    setTimeout update, updateInterval
    return

  setupDivs()
  data = [
    []
    []
    []
    []
    []
    []
    []
    []
    []
    []
  ]
  getData()
  plot = $.plot('#placeholder', dataAccordingToChoices(),
    series: shadowSize: 0
    yaxis:
      min: MIN_VALUE
      max: MAX_VALUE
    xaxis: show: false)
  update()
  return

# ---
# generated by js2coffee 2.0.3