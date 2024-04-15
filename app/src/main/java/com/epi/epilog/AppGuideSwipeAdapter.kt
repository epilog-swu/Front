package com.epi.epilog

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AppGuideSwipeAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity){

    //AppGuideSwipeActivity에 여러가지 화면을 보여줘야 하므로
    //어댑터에 그 화면들을 담을 수 있는 FragmentList 생성
    private val fragmentlist : ArrayList<Fragment> = ArrayList()


    override fun getItemCount(): Int  = fragmentlist.size

    override fun createFragment(position: Int): Fragment = fragmentlist[position]

    //프래그먼트 리스트에 프래그먼트 추가
    fun addFragment(fragment: Fragment){
        fragmentlist.add(fragment)
        //아이템이 추가되었다고 Adapter에게 알림.
        notifyItemInserted(fragmentlist.size-1)
    }
}