var updateInterval = 10;
var totalPoints = 100;



var brainWavesQueue = []


var datasets = {
  "attention" : {
    label : "attention",
    color : 0
  },
  "meditation" : {
    label : "meditation",
    color : 1
  },
  "delta" : {
    label : "delta",
    color : 2
  },
  "theta" : {
    label : "theta",
    color : 3
  },
  "low alpha" : {
    label : "low alpha",
    color : 4
  },
  "high alpha" : {
    label : "high alpha",
    color : 5
  },
  "low beta" : {
    label : "low beta",
    color : 6
  },
  "high beta" : {
    label : "high beta",
    color : 7
  },
  "low gamma" : {
    label : "low gamma",
    color : 8
  },
  "high gamma" : {
    label : "high gamma",
    color : 9
  }
}


$(function() {
  setupDivs();

	var data = [
	  [],
	  [],
	  [],
	  [],
	  [],
	  [],
	  [],
	  [],
	  []
	];
		

	function getData() {
    var res = [
       [],
       [],
       [],
       [],
       [],
       [],
       [],
       [],
       []
    ];
    if(brainWavesQueue.length > 0){
       // most of the time this will not happen, queue.length will be 0
       var brainFrame = brainWavesQueue[0]
       brainWavesQueue = brainWavesQueue.slice(1)
       for(var i = 0; i < data.length; i++){
           data[i]  = data[i].slice(1)
        }
         // and push the brainwave onto each one 
         data[0].push(brainFrame.attention)
         data[1].push(brainFrame.meditation)
         data[2].push(brainFrame.delta)
         data[3].push(brainFrame.lowAlpha)
         data[4].push(brainFrame.highAlpha)
         data[5].push(brainFrame.lowBeta)
         data[6].push(brainFrame.highBeta)
         data[7].push(brainFrame.lowGamma)
         data[8].push(brainFrame.highGamma)
    } else {
      // if you have no new brainwaves, slice and push previous value (just to keep things *moving*, you know)
      for(var i = 0; i < data.length; i++){
        if (data[i].length > 0)
             data[i] = data[i].slice(1);
        while (data[i].length < totalPoints) {
          var prev = data[i].length > 0 ? data[i][data[i].length - 1] : 1.0 ; // push previous value, default to 1.0
          data[i].push(prev);
        }
        // Zip the generated y values with the x values
        for (var j = 0; j < data[i].length; j++) {
             res[i].push([j, data[i][j]])
        }
      }
    }
    
    for(var i = 0; i < res.length; i++) {
      datasets[getLabel(i)].data = res[i]  
    }
  }
  
  function getLabel(i){
    var label = ""
    switch(i){
      case 0:
      label = "attention" 
      break;
      case 1:
      label = "meditation" 
      break;
      case 2:
      label = "delta" 
      break;
      case 3:
      label = "theta" 
      break;
      case 4:
      label = "low alpha" 
      break;
      case 5:
      label = "high alpha" 
      break;
      case 6:
      label = "low beta" 
      break;
      case 7:
      label = "high beta" 
      break;
      case 8:
      label = "low gamma" 
      break;
      case 9:
      label = "high gamma" 
      break;
    }
    return label
  }
	  
	
  getData();
  var plot = $.plot("#placeholder", dataAccordingToChoices() , {
   series: {
     shadowSize: 0 // Drawing is faster without shadows
   },
   yaxis: {
     min: 0.5,
     max: 1.5
   },
   xaxis: {
     show: false
   }
  });

	function update() {
	  getData();
		plot.setData(dataAccordingToChoices());

		// Since the axes don't change, we don't need to call plot.setupGrid()

		plot.draw();
		setTimeout(update, updateInterval);

	}

  update();  
});

function dataAccordingToChoices() {
  var choiceContainer = $("#choices");
	var data = [];

	choiceContainer.find("input:checked").each(function () {
		var key = $(this).attr("name");
		if (key && datasets[key]) {
			data.push(datasets[key]);
		}
	});
	return data
}


function setupDivs(){
  
  var choiceContainer = $("#choices");
	$.each(datasets, function(key, val) {
		choiceContainer.append("<br/><input type='checkbox' name='" + key +
			"' checked='checked' id='id" + key + "'></input>" +
			"<label for='id" + key + "'>"
			+ val.label + "</label>");
	});
  
  $("#footer").prepend("Flot " + $.plot.version + " &ndash; ");
}
