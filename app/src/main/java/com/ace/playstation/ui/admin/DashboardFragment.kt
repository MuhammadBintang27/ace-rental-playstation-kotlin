package com.ace.playstation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ace.playstation.databinding.FragmentAdminDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get user information from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession",
            android.content.Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "")

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}