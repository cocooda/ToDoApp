package com.example.todoapp.ui.tasklist

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.viewmodel.TaskViewModelFactory
import com.example.todoapp.databinding.FragmentTaskListBinding
import com.example.todoapp.databinding.LayoutMenuFilterBinding
import com.example.todoapp.databinding.LayoutMenuSortBinding
import com.example.todoapp.di.AppDatabaseProvider
import com.example.todoapp.ui.common.TaskAdapter
import com.example.todoapp.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.RecyclerView

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskAdapter
    private lateinit var viewModel: TaskViewModel

    // This list holds the tasks currently displayed (filtered/searched/sorted)
    private var currentDisplayedTasks = listOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupRecyclerView()
        observeTasks()
        setupUI()
    }

    private fun setupViewModel() {
        val database = AppDatabaseProvider.getDatabase(requireContext())
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[TaskViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter { task ->
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditFragment(task.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewTasks.adapter = adapter

        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false // no move support

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val taskToDelete = adapter.currentList[position]
                // Ask ViewModel to delete
                viewModel.deleteTask(taskToDelete)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewTasks)
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.allTasks.collect { tasks ->
                    currentDisplayedTasks = tasks
                    adapter.submitList(currentDisplayedTasks)
                }
            }
        }
    }

    private fun setupUI() {
        binding.fabAddTask.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditFragment()
            findNavController().navigate(action)
        }

        binding.ivSort.setOnClickListener {
            showPopupMenuSort(it)
        }

        binding.ivFilter.setOnClickListener {
            showPopupMenuFilter(it)
        }

        binding.edtInputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWithSearch(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterWithSearch(query: String) {
        val filtered = if (query.isEmpty()) {
            currentDisplayedTasks
        } else {
            currentDisplayedTasks.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.submitList(filtered)
    }

    private fun showPopupMenuSort(anchor: View) {
        val popupBinding = LayoutMenuSortBinding.inflate(LayoutInflater.from(requireContext()))
        val popup = PopupWindow(popupBinding.root, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.elevation = 100f

        popupBinding.lnAZ.setOnClickListener {
            val sorted = currentDisplayedTasks.sortedBy { it.title }
            adapter.submitList(sorted)
            popup.dismiss()
        }

        popupBinding.lnZA.setOnClickListener {
            val sorted = currentDisplayedTasks.sortedByDescending { it.title }
            adapter.submitList(sorted)
            popup.dismiss()
        }

        showSmartPopup(popup, anchor)
    }

    private fun showPopupMenuFilter(anchor: View) {
        val popupBinding = LayoutMenuFilterBinding.inflate(LayoutInflater.from(requireContext()))
        val popup = PopupWindow(popupBinding.root, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.elevation = 100f

        popupBinding.lnAll.setOnClickListener {
            adapter.submitList(currentDisplayedTasks)
            popup.dismiss()
        }

        popupBinding.lnLow.setOnClickListener {
            val filtered = currentDisplayedTasks.filter { it.priority == 0 }
            adapter.submitList(filtered)
            popup.dismiss()
        }

        popupBinding.lnMedium.setOnClickListener {
            val filtered = currentDisplayedTasks.filter { it.priority == 1 }
            adapter.submitList(filtered)
            popup.dismiss()
        }

        popupBinding.lnHigh.setOnClickListener {
            val filtered = currentDisplayedTasks.filter { it.priority == 2 }
            adapter.submitList(filtered)
            popup.dismiss()
        }

        showSmartPopup(popup, anchor)
    }

    private fun showSmartPopup(popup: PopupWindow, anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        anchor.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popup.contentView.measuredHeight

        if (location[1] + anchor.height + popupHeight > screenHeight) {
            popup.showAsDropDown(anchor, -350, -(popupHeight + anchor.height), Gravity.NO_GRAVITY)
        } else {
            popup.showAsDropDown(anchor, -350, 30, Gravity.NO_GRAVITY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
