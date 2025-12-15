package com.example.basefragment.core.base


import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.helper.SharedPreferencesManager
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import kotlin.getValue

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel>(  private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
                                                                  private val viewModelClass: Class<VM>) : Fragment() {

    private lateinit var _binding: VB
    protected val binding: VB get() = _binding
    protected val viewModelActivity: ViewModelActivity by activityViewModels()
    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[viewModelClass]
    }
    open fun setupPreViews() {}
    abstract fun viewListener()
    protected var toast: Toast? = null
    @Inject
    internal lateinit var sharedPreferences: SharedPreferencesManager
    abstract fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView: $this")
        _navController = findNavController()
        _binding = inflateBinding(inflater, container, savedInstanceState)
        setupPreViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated: $this")
        initView()
        initText()
        observeData()

        viewListener()

        bindViewModel()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.v(TAG, "onAttach: $this")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate: $this")
    }
    override fun onStart() {
        super.onStart()
        Log.v(TAG, "onStart: $this")
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume: $this")
    }

    override fun onPause() {
        super.onPause()
        Log.v(TAG, "onPause: $this")
    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "onStop: $this")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.v(TAG, "onDestroyView: $this")
        _navController = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy: $this")
    }

    override fun onDetach() {
        super.onDetach()
        Log.v(TAG, "onDetach: $this")
    }
    private var _navController: NavController? = null

    protected val navController: NavController? get() = _navController
    open fun initView() {}
    open fun initText() {}
    open fun observeData() {}

    fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    fun showToast(content: Any) {
        if (toast != null){
            toast?.cancel()
        }
        val contentString = when (content) {
            is String -> content
            is Int -> getString(content)
            else -> {""}
        }
        toast = Toast.makeText(requireContext(), contentString, Toast.LENGTH_SHORT)
        toast?.show()
    }
    abstract fun bindViewModel()
}