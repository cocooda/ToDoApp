package com.example.todoapp.ui.tasklist

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.data.repository.TaskViewModelFactory
import com.example.todoapp.databinding.FragmentTaskListBinding
import com.example.todoapp.databinding.LayoutMenuFilterBinding
import com.example.todoapp.databinding.LayoutMenuSortBinding
import com.example.todoapp.di.AppDatabaseProvider
import com.example.todoapp.ui.common.TaskAdapter
import com.example.todoapp.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskAdapter

    private lateinit var viewModel: TaskViewModel
    private var filteredList = arrayListOf<Task>()

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
        setupFab()
    }

    private fun setupViewModel() {
        val database = AppDatabaseProvider.getDatabase(requireContext())
        val taskDao = database.taskDao()
        val repository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]
    }

    private val sampleTasks = listOf(
        Task(id = 1, title = "Do homework", priority = 0, isCompleted = false, dueDate = System.currentTimeMillis()),
        Task(id = 2, title = "AAA", priority = 0, isCompleted = false, dueDate = null),
        Task(id = 3, title = "BBB", priority = 0, isCompleted = false, dueDate = null),
        Task(id = 4, title = "CCCC", priority = 1, isCompleted = false, dueDate = null),
        Task(id = 5, title = "DDD", priority = 1, isCompleted = false, dueDate = null),
        Task(id = 6, title = "EEE", priority = 1, isCompleted = false, dueDate = null),
        Task(id = 7, title = "ffff", priority = 2, isCompleted = false, dueDate = null),
        Task(id = 8, title = "gggg", priority = 2, isCompleted = false, dueDate = null),
        Task(id = 9, title = "Read a book", priority = 2, isCompleted = true, dueDate = System.currentTimeMillis())
    )

    private fun setupRecyclerView() {
        adapter = TaskAdapter()
        binding.recyclerViewTasks.adapter = adapter
        adapter.submitList(sampleTasks)
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allTasks.collect { tasks ->
                    //adapter.submitList(tasks)
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditFragment()
            findNavController().navigate(action)

        }

        binding.apply {
            ivSort.setOnClickListener {
                showPopupMenuSort(ivSort)
            }

            ivFilter.setOnClickListener {
                showPopupMenuFilter(ivFilter)
            }

            edtInputSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filterWithSearch(s.toString().trim())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

        }
    }

    private fun filterWithSearch(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(sampleTasks)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (item in sampleTasks) {
                if (item.title?.lowercase()?.contains(lowerCaseQuery) == true) {
                    filteredList.add(item)
                }
            }
        }
        adapter.submitList(filteredList.toMutableList())
    }

    private fun showPopupMenuSort(view: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val binding1 = LayoutMenuSortBinding.inflate(layoutInflater)
        val popupMenu = PopupWindow(requireContext())
        popupMenu.contentView = binding1.root
        popupMenu.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupMenu.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupMenu.isFocusable = true
        popupMenu.isOutsideTouchable = true
        popupMenu.elevation = 100f

        popupMenu.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Đo kích thước của PopupWindow để tính toán chiều cao cần thiết
        binding1.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = binding1.root.measuredHeight

        // Lấy vị trí của view gốc trên màn hình
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val yPos = location[1] + view.height // Vị trí y của view gốc
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        // Kiểm tra nếu không gian phía dưới không đủ để hiển thị toàn bộ PopupWindow
        if (yPos + popupHeight > screenHeight) {
            // Hiển thị PopupWindow phía trên view gốc nếu không đủ không gian bên dưới
            popupMenu.showAsDropDown(view, -350, -(popupHeight + view.height), Gravity.NO_GRAVITY)
        } else {
            // Hiển thị PopupWindow phía dưới view gốc nếu đủ không gian
            popupMenu.showAsDropDown(view, -350, 30, Gravity.NO_GRAVITY)
        }

        binding1.lnAZ.setOnClickListener {
            val list = sampleTasks.sortedBy { it.title }
            adapter.submitList(list.toList())
            popupMenu.dismiss()
        }

        binding1.lnZA.setOnClickListener {
            val list = sampleTasks.sortedByDescending { it.title }
            adapter.submitList(list.toList())
            popupMenu.dismiss()
        }

        popupMenu.showAsDropDown(view, -350, 30, Gravity.NO_GRAVITY)
    }

    private fun showPopupMenuFilter(view: View) {
        val layoutInflater = LayoutInflater.from(requireContext())
        val binding1 = LayoutMenuFilterBinding.inflate(layoutInflater)
        val popupMenu = PopupWindow(requireContext())
        popupMenu.contentView = binding1.root
        popupMenu.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupMenu.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupMenu.isFocusable = true
        popupMenu.isOutsideTouchable = true
        popupMenu.elevation = 100f

        popupMenu.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Đo kích thước của PopupWindow để tính toán chiều cao cần thiết
        binding1.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = binding1.root.measuredHeight

        // Lấy vị trí của view gốc trên màn hình
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val yPos = location[1] + view.height // Vị trí y của view gốc
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        // Kiểm tra nếu không gian phía dưới không đủ để hiển thị toàn bộ PopupWindow
        if (yPos + popupHeight > screenHeight) {
            // Hiển thị PopupWindow phía trên view gốc nếu không đủ không gian bên dưới
            popupMenu.showAsDropDown(view, -350, -(popupHeight + view.height), Gravity.NO_GRAVITY)
        } else {
            // Hiển thị PopupWindow phía dưới view gốc nếu đủ không gian
            popupMenu.showAsDropDown(view, -350, 30, Gravity.NO_GRAVITY)
        }

        binding1.lnAll.setOnClickListener {
            val list = sampleTasks
            adapter.submitList(list.toList())
            popupMenu.dismiss()
        }

        binding1.lnLow.setOnClickListener {
            val list = sampleTasks.filter { it.priority == 0 }
            adapter.submitList(list.toList())
            popupMenu.dismiss()
        }

        binding1.lnMedium.setOnClickListener {
            val list = sampleTasks.filter { it.priority == 1 }
            adapter.submitList(list.toList())
            popupMenu.dismiss()
        }

        binding1.lnHigh.setOnClickListener {
            val list = sampleTasks.filter { it.priority == 2 }
            adapter.submitList(list.toList())
            popupMenu.dismiss()
        }

        popupMenu.showAsDropDown(view, -350, 30, Gravity.NO_GRAVITY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
