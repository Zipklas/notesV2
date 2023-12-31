package com.example.notesv2.Adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.notesv2.Models.NoteModel
import com.example.notesv2.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak
import java.text.SimpleDateFormat

class NoteAdapter(var options:FirestoreRecyclerOptions<NoteModel>, var context: Context) : FirestoreRecyclerAdapter<NoteModel, NoteAdapter.NoteViewHolder>(options) {


    class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val contextCard: CardView = itemView.findViewById(R.id.noteItemLayoutParent)
        val noteContent: TextView = itemView.findViewById(R.id.noteContentItem)
        val noteTitle: TextView = itemView.findViewById(R.id.noteItemTitle)
        val noteDate: TextView = itemView.findViewById(R.id.noteData)

        val markWon= Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin(){
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ){ visitor, _ ->visitor.forceNewLine()}

                }
            }).build()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.note_layout,parent,false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, model: NoteModel) {


        holder.apply {

            markWon.setMarkdown(noteContent, model.content)
            noteTitle.text = model.title
            contextCard.setCardBackgroundColor(model.color)
            noteDate.text = SimpleDateFormat.getInstance().format(model.date)


            if (model.content =="") {

                contextCard.visibility = View.GONE

            }

            else if (noteContent.text.isEmpty()){

                contextCard.visibility = View.GONE

            }


            holder.contextCard.setOnClickListener {

                val bundle = Bundle()
                bundle.putString("noteId", NoteAdapter(options,context).snapshots.getSnapshot(position).id)
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveEditFragment, bundle)

            }

            holder.noteTitle.setOnClickListener {

                val bundle = Bundle()
                bundle.putString("noteId", NoteAdapter(options,context).snapshots.getSnapshot(position).id)
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveEditFragment, bundle)

            }

            holder.noteContent.setOnClickListener {

                val bundle = Bundle()
                bundle.putString("noteId", NoteAdapter(options,context).snapshots.getSnapshot(position).id)
                Navigation.findNavController(it)
                    .navigate(R.id.action_noteFragment_to_saveEditFragment, bundle)

            }

        }


    }

}