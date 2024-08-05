package api
import org.json.JSONObject


interface DiaryFragment {
    fun getData(): JSONObject
    fun isFilledOut(): Boolean
}
