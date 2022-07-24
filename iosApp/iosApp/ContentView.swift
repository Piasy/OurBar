import SwiftUI
import OurBar

struct ContentView: View {
	let greet = "hello"
    
	var body: some View {
		Text(greet)
	}
    
    init() {
        OurBarTest().test()
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
