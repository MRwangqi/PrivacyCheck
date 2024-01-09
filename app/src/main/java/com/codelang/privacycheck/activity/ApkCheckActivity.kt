package com.codelang.privacycheck.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codelang.privacycheck.R
import com.codelang.runtimecheck.RuntimeCheck
import com.codelang.runtimecheck.bean.StackLog
import java.text.SimpleDateFormat
import java.util.Date

class ApkCheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apk_check)

        val rv = findViewById<RecyclerView>(R.id.rvList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ApkCheckAdapter(RuntimeCheck.stackList)
    }


}

class ApkCheckAdapter(private val list: List<StackLog>) :
    RecyclerView.Adapter<ApkCheckViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApkCheckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check_info, parent, false)
        return ApkCheckViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ApkCheckViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            holder.tvStack.visibility =
                if (holder.tvStack.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val date = Date(list[position].time)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS")
        val formattedDate: String = dateFormat.format(date)

        holder.tvTime.text = formattedDate
        holder.tvMethod.text = list[position].method
        holder.tvStack.text = list[position].stack
    }
}


class ApkCheckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvTime: TextView = itemView.findViewById<TextView>(R.id.tvTime)
    val tvMethod: TextView = itemView.findViewById<TextView>(R.id.tvMethod)
    val tvStack: TextView = itemView.findViewById<TextView>(R.id.tvStack)
}