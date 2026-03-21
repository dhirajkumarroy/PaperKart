package com.example.paperkart.features.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.CartApi
import com.example.paperkart.databinding.FragmentCartBinding

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private lateinit var viewModel: CartViewModel
    private lateinit var adapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCartBinding.bind(view)

        val api = RetrofitClient.getInstance(requireContext()).create(CartApi::class.java)
        val repo = CartRepository(api)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CartViewModel(repo) as T
            }
        })[CartViewModel::class.java]

        adapter = CartAdapter { item ->
            viewModel.removeItem(item.product.id, item.sku)
        }

        binding.rvCart.layoutManager = LinearLayoutManager(context)
        binding.rvCart.adapter = adapter

        viewModel.cart.observe(viewLifecycleOwner) {
            adapter.submitList(it?.items ?: emptyList())
        }

        viewModel.totalPrice.observe(viewLifecycleOwner) {
            binding.tvTotal.text = "₹$it"
        }

        viewModel.loadCart()
    }
}