class PermissionModel {
    var granted = false
        private set
    var rationale = false
        private set
    var dialogShows = true
        private set

    private var denialCount = 0

    fun request(userAllows: Boolean) {
        if (!dialogShows) {
            println("no dialog, denied immediately")
            return
        }

        if (userAllows) {
            granted = true
            rationale = false
        } else {
            denialCount += 1
            granted = false
            rationale = denialCount == 1
            if (denialCount >= 2) {
                dialogShows = false
            }
        }
    }
}

fun main() {
    val model = PermissionModel()
    val requests = listOf(false, false, false, true)

    requests.forEachIndexed { index, userAllows ->
        println("Request ${index + 1}")
        model.request(userAllows)
        println("granted=${model.granted}")
        println("rationale=${model.rationale}")
        println("dialogShows=${model.dialogShows}")
    }
}
