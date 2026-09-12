package com.example.agenciaviajes.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agenciaviajes.databinding.ItemDestinoBinding
import com.example.agenciaviajes.models.Destino
import com.example.agenciaviajes.utils.DriveUploader

class DestinoAdapter(
    private var lista: List<Destino>,
    private val onItemClick: (Destino) -> Unit,
    private val onItemLongClick: (Destino) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.VH>() {

    inner class VH(val binding: ItemDestinoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDestinoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val destino = lista[position]

        Glide.with(holder.itemView.context)
            .load(DriveUploader.construirUrlImagen(destino.imagenDriveId))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_delete)
            .into(holder.binding.ivDestino)

        holder.binding.tvNombre.text = destino.nombre
        holder.binding.tvPais.text = destino.pais
        holder.binding.tvPrecio.text = "$${destino.precio}"
        holder.binding.tvDescripcion.text = destino.descripcion

        holder.itemView.setOnClickListener { onItemClick(destino) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(destino)
            true
        }
    }
    override fun getItemCount() = lista.size

    fun actualizarLista(nueva: List<Destino>) {
        lista = nueva
        notifyDataSetChanged()
    }
}