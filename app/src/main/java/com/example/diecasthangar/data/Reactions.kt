package com.example.diecasthangar.data

import com.example.diecasthangar.R

object Reactions{
    val reactionsList: ArrayList<Reaction> = ArrayList()
    init{
        reactionsList.add(Reaction("plane", R.drawable.ic_airplane_black_48dp))
        reactionsList.add(Reaction("takeoff", R.drawable.ic_airplane_takeoff_black_48dp))
        reactionsList.add(Reaction("plane", R.drawable.ic_airplane_landing_black_48dp))
        reactionsList.add(Reaction("fire", R.drawable.ic_fire_black_48dp))
    }

    class ReactionsList {
        init {


        }

    }

}
