package com.test.gradledemoapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.test.gradledemoapp.databinding.FragmentFirstBinding
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            val setp = try {
                binding.textviewFirst.text.toString().toLong()
            } catch (e: Exception) {
                50L
            }
            testAAA(setp)
        }
        binding.button2.setOnClickListener {
            val setp = try {
                binding.textviewFirst.text.toString().toLong()
            } catch (e: Exception) {
                50L
            }
            thread {
                testBBB(setp)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun testAAA(step: Long) {
        val endTime = System.currentTimeMillis() + step
        while (System.currentTimeMillis() < endTime) {

        }
        Log.e("methodTag", "退出循环了。。。")
    }

    private fun testBBB(step: Long) {
        val endTime = System.currentTimeMillis() + step
        while (System.currentTimeMillis() < endTime) {

        }
        Log.e("methodTag", "退出循环了。。。")
    }
}