updateInterval = 30
totalPoints = 100
MIN_VALUE = -.5
MAX_VALUE = 2
REDRAW_THRESHOLD = .2

brainWavesQueue = []
labels = [
  'attention',
  'meditation',
  'delta',
  'theta',
  'low alpha',
  'high alpha',
  'low beta',
  'high beta',
  'low gamma',
  'high gamma'
]

datasets = (new -> @[l] = (new -> @["label"] = l; @["color"] = index; @['isChecked'] = true; @;) for l,index in  labels; @)

data = ([] for l in labels)

