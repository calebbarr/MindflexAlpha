getData = ->
  res = ([] for l in @labels)
  
  pushBrainFrameToData = ->
    brainFrame = @brainWavesQueue[0]
    @brainWavesQueue = @brainWavesQueue.slice(1)
    for datum,i in @data 
      do ->
        @data[i] = @data[i].slice(1)
        @data[i].push brainFrame[@labels[i]]
        
  fillOutResult = ->
    for datum,i in @data
      do ->
        datum = if datum.length > 0 then datum.slice(1) else datum
        while datum.length < @totalPoints
          # push previous value, default to 1.0
          prev = if datum.length > 0 then datum[datum.length - 1] else 1.0
          datum.push prev
        @data[i] = datum
        for thing,j in datum
          do ->
            res[i].push [
              j
              @data[i][j]
            ]
  
  if @brainWavesQueue.length > 0
    # most of the time this will not happen, queue.length will be 0
    pushBrainFrameToData()
  else
    fillOutResult()
  for thing,i in res
    do ->
      @datasets[@labels[i]].data = res[i]
  return

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

$ ->
  setupDivs()
  getData()
  plot = $.plot('#placeholder', dataAccordingToChoices(),
    series: shadowSize: 0
    yaxis:
      min: MIN_VALUE
      max: MAX_VALUE
    xaxis: show: false)
  update = ->
    getData()
    plot.setData dataAccordingToChoices()
    # if(newMinOrMax())
    #   plot.setupGrid();
    plot.draw()
    setTimeout update, updateInterval
    return
  update()
  return