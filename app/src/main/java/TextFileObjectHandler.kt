package com.example.listsqre_revamped

class TextFileObjectHandler {
    class CardDetailItem(
        private var id: Int,
        private var title: String = "",
        private var description: String = "",
        private var isSelected: Boolean = false,
        private var isPinned: Boolean = false
    ) {
        // getter functions
        fun GetId_(): Int { return id }
        fun GetTitle_(): String { return title }
        fun GetDescription_(): String { return description }
        fun GetSelectStatus_(): Boolean { return isSelected }
        fun GetPinStatus_(): Boolean { return isPinned }

        // setter functions
        fun SetId_(newId: Int) { id = newId }
        fun SetTitle_(newTitle: String) { title = newTitle }
        fun SetDescription_(newDescription: String) { description = newDescription }
        fun SetSelectStatus_(newSelectStatus: Boolean) { isSelected = newSelectStatus }
        fun SetPinStatus_(newPinStatus: Boolean) { isPinned = newPinStatus }

        // process data into string format for writing to file
        fun ItemDelimiterString(): String {
            var data = ""
            data += id.toString() + GlobalVar.DELIMITER
            data += title + GlobalVar.DELIMITER
            data += description + GlobalVar.DELIMITER
            data += if(isPinned){
                "1" + GlobalVar.DELIMITER
            } else {
                "0" + GlobalVar.DELIMITER
            }
            data += GlobalVar.ITEM_DELIMITER
            return data
        }
    }

    // CRUD operations and other backend processes
    // note: companion object contents are static
    companion object {
        private var idGen: Int = 0
        private var mutableList = mutableListOf<CardDetailItem>()
        private var selectedList = mutableListOf<CardDetailItem>()
        var empty: Boolean = true

        fun AddItem(title: String, description: String, isPinned: Boolean) {
            mutableList.add(CardDetailItem(idGen++, title, description, false, isPinned))
            empty = false
        }

        private fun DeleteItem(id: Int) {
            idGen--
            mutableList.removeAt(id)
            ReassignItemID()
            if(mutableList.isEmpty()) {
                idGen = 0
                empty = true
            } else { /* do nothing */ }
        }

        // prevent double creation when start new activity
        fun DeleteAllItems() {
            idGen = 0
            mutableList.clear()
            empty = true
        }

        fun PushToSelList(id: Int) {
            selectedList.add(mutableList[id])
        }

        fun RemoveFromSelList(itemToRemove: CardDetailItem) {
            if(selectedList.contains(itemToRemove)) {
                selectedList.remove(itemToRemove)
            } else { /* do nothing */ }
        }

        fun GetEntireList(): List<CardDetailItem> {
            return mutableList.toList()
        }

        fun GetEntireSelList(): List<CardDetailItem> {
            return selectedList.toList()
        }

        fun DeleteSelNodes() {
            for(item in selectedList) {
                DeleteItem(item.GetId_())
            }
            selectedList.clear()
        }

        fun ClearSelList() {
            selectedList.clear()
        }

        private fun ReassignItemID() {
            for((iterator, obj) in GetEntireList().withIndex()) {
                obj.SetId_(iterator)
            }
        }
    }
}