var button = document.getElementById("startButton");
button.addEventListener("click", first)
function first() {
	var divToHide = document.getElementById("startForm"); 
	var showDiv = document.getElementById("gameBoard");
    divToHide.style.display = 'none';
    showDiv.style.display = 'block';


	console.log("STARTED");

	//pull number of symbols from form input
	var numberOfSymbols = document.getElementById("numSymbols").value;
	if(numberOfSymbols > 8){
		numberOfSymbols = 8;
	}


	var generateSymbols = createTable();
	var reduced = generateSymbols.splice(0,numberOfSymbols);
	var endIndex = reduced.length;

	var u = document.getElementById('magicNumber');
	u.setAttribute("data-classified", endIndex);

	//duplicate elements in array because every symbols needs a pair
	for(var g =0; g < endIndex; g++){
		reduced.push(reduced[g]);
	}

	var table = document.getElementById("tbl");
	var items = table.childNodes;

	var count = 0;
	var symbolArray = [];

	var indexI;
	var indexJ;
	if(endIndex === 8){
		indexJ=4;
		indexI=4
	}
	else if(endIndex === 2){
		indexI=2;
		indexJ=2;
	}
	else{
		indexI = endIndex;
		indexJ = 2;
	}

	for(var i=0; i<indexI;i++){
		var row = document.createElement('tr');
		for(var j = 0; j < indexJ; j++){
			var indexOfSymbol;
			var symbol;
			var totalGuesses=0;
		    var columnInRow = document.createElement('td');

		   	if(reduced.length === 1){
				indexOfSymbol = 0;
				symbol = reduced[indexOfSymbol];
				symbolArray.push(symbol);
			}

			else{
				indexOfSymbol = Math.floor(Math.random() * reduced.length);
				symbol = reduced[indexOfSymbol];
				reduced.splice(indexOfSymbol,1);
				symbolArray.push(symbol);
			}
		 
		    //set secret data attribute with index
		    columnInRow.setAttribute("data-classified", symbol);

		    var guessTracker = document.getElementById("guess");
		    var matchTracker = document.getElementById("pairCount");
		    var totalGuesses = 0;
		    var countObj = 0;

		    guessTracker.setAttribute("data-classified", totalGuesses);
		    matchTracker.setAttribute("data-classified", countObj);

			console.log("HERE", count, symbolArray);

			columnInRow.addEventListener("click", makeHandler);
			columnInRow.addEventListener("mouseover", makeCool);

			count++;
		    row.appendChild(columnInRow);			
		}
		table.appendChild(row);
	}//end outer for



	document.body.appendChild(table);
};

function makeHandler(){

	//event listender for every button
	var guessTracker = document.getElementById("guess");
	var totalGuesses = guessTracker.getAttribute("data-classified");
	var tempCard = document.getElementById("flipIndex");
	var matchObj = document.getElementById("pairCount");
	var magicNumberCount = document.getElementById("magicNumber");

		
	if(totalGuesses%2 == 0 ){
		totalGuesses++;
		var flippedCardsSymbol = this.getAttribute("data-classified");
		tempCard.setAttribute("data-classified", flippedCardsSymbol);

		this.innerHTML = this.getAttribute("data-classified");
		guessTracker.setAttribute("data-classified", totalGuesses);
	}

	else{
		totalGuesses++;
		var flippedCard = document.getElementById("flipIndex");
		//flippedCard.setAttribute("data-classified", this);
		guessTracker.setAttribute("data-classified", totalGuesses);
		this.innerHTML = this.getAttribute("data-classified");

		var toCheck = tempCard.getAttribute("data-classified");
		
		if(toCheck === this.getAttribute("data-classified")){
			tempCard.setAttribute("data-classified", "");
			var x = matchObj.getAttribute("data-classified");
			x = +x + 1;
			var toWin = magicNumberCount.getAttribute("data-classified");
			matchObj.setAttribute("data-classified", x);


			//determine whether card was a match--and render HTML accordingly
			var cells = document.getElementsByTagName('td');
			Array.prototype.forEach.call(cells, function( cell ) {
			if ( cell.getAttribute("data-classified") == toCheck){
				//this.click=function(){null}
				cell.removeEventListener('click', makeHandler);
				cell.style.background = "red";
				}
			});

			/**
			CLEAR TABLE, RESET HTML ELEMENTS
			**/
			if(x == toWin){
				
    			alert("You won! Thanks for playing!");
 
				var hide = document.getElementById("gameBoard");
    			
				var myNode = document.getElementById("tbl");
				while (myNode.firstChild) {
    				myNode.removeChild(myNode.firstChild);
				}
					guessTracker.innerHTML = "";
					tempCard.setAttribute("data-classified", "");
					guessTracker.setAttribute("data-classified", "");
					matchObj.setAttribute("data-classified", "");
					magicNumberCount.setAttribute("data-classified", "");
					totalGuesses=0;

    			first();
			}
		}

		else{

			this.innerHTML = "";
			var cells = document.getElementsByTagName('td');
			Array.prototype.forEach.call(cells, function( cell ) {
			if ( cell.getAttribute("data-classified") == toCheck){
				cell.innerHTML = "";
				tempCard.setAttribute("data-classified", "");
				}
			});


		}//end small

			
		guessTracker.innerHTML = (totalGuesses)/2 + " guesses";
		

	}//end big else

}

function makeCool(){
	//this.style.background = "green";
}

var addToScore = function(){
	console.log("AY");
}

function createTable(){
	var symbolTable = [];
	symbolTable.push('☃');
	symbolTable.push('\2603');
	symbolTable.push('\2604');
	symbolTable.push('\2605');
	symbolTable.push('\2606');
	symbolTable.push('\2607');
	symbolTable.push('\2608');
	symbolTable.push('\2609');
	return symbolTable;
}
