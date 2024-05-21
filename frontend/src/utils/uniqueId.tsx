  
function sleep(milliseconds:any) {
	var start = new Date().getTime();
	for (var i = 0; i < 1e7; i++) {
		if ((new Date().getTime() - start) > milliseconds){
			break;
		}
	}
}

export default (prefix:any) => { 
	let id = new Date().valueOf().toString(36);
	sleep(1);
	return (prefix ? prefix + id	: id);
};