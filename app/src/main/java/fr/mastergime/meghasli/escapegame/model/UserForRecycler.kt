package fr.mastergime.meghasli.escapegame.model

import androidx.recyclerview.widget.DiffUtil

data class UserForRecycler(val name: String, val ready : Boolean) {

    class DiffCallback : DiffUtil.ItemCallback<UserForRecycler>() {
        override fun areItemsTheSame(oldItem: UserForRecycler, newItem: UserForRecycler): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: UserForRecycler,
            newItem: UserForRecycler
        ): Boolean {
            return oldItem.name == newItem.name  && oldItem.ready == newItem.ready
        }

    }
}