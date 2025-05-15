import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.model.admin.Pengeluaran

class PengeluaranAdapter(private val data: MutableList<Pengeluaran>) :
    RecyclerView.Adapter<PengeluaranAdapter.PengeluaranViewHolder>() {

    inner class PengeluaranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.tvNamaPengeluaranAdmin)
        val jumlah: TextView = itemView.findViewById(R.id.tvJumlahPengeluaranAdmin)
        val kategori: TextView = itemView.findViewById(R.id.tvKategoriPengeluaranAdmin)
        val nominal: TextView = itemView.findViewById(R.id.tvPengeluaranAmountAdmin)
        val tanggal: TextView = itemView.findViewById(R.id.tvTanggalAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengeluaranViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengeluaran_admin, parent, false)
        return PengeluaranViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PengeluaranViewHolder, position: Int) {
        val item = data[position]
        holder.nama.text = item.nama
        holder.kategori.text = item.kategori
        holder.nominal.text = "Rp %,d".format(item.jumlah.toInt())
        holder.tanggal.text = item.tanggal

        if (item.kategori.equals("Lainnya", ignoreCase = true)) {
            holder.jumlah.visibility = View.GONE
        } else {
            holder.jumlah.visibility = View.VISIBLE
            holder.jumlah.text = "Stok baru: ${item.stok_tambahan}"
        }
    }


    fun updateData(pengeluaranList: List<Pengeluaran>) {
        data.clear()
        data.addAll(pengeluaranList)
        notifyDataSetChanged()
    }
}
