package com.example.todoapp.ui.addedit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.todoapp.R
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.databinding.FragmentAddEditTaskBinding
import com.example.todoapp.di.AppDatabaseProvider
import com.example.todoapp.viewmodel.TaskViewModel
import com.example.todoapp.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TaskViewModel

    private var selectedDueDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedHour: Int = 9
    private var selectedMinute: Int = 0

    private var currentTask: Task? = null

    private val args: AddEditTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupPrioritySpinner()

        val taskId = args.taskId
        if (taskId != -1) {
            loadTask(taskId)
            binding.btnSaveTask.text = getString(R.string.update_task)
        } else {
            binding.btnSaveTask.text = getString(R.string.add_task)
        }

        binding.btnSelectDate.setOnClickListener { showDatePickerDialog() }
        binding.btnSaveTask.setOnClickListener { saveOrUpdateTask() }
        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewModel() {
        val database = AppDatabaseProvider.getDatabase(requireContext())
        val taskDao = database.taskDao()
        val repository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(repository)

        viewModel = ViewModelProvider(requireActivity(), factory)[TaskViewModel::class.java]
    }

    private fun setupPrioritySpinner() {
        val priorities = resources.getStringArray(R.array.priority_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter
    }

    private fun loadTask(taskId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val task = viewModel.getTaskByIdOnce(taskId)
            if (task != null) {
                currentTask = task
                populateUI(task)
            } else {
                Toast.makeText(requireContext(), "Task ID: $taskId not found", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun populateUI(task: Task) {
        binding.etTaskTitle.setText(task.title)
        binding.spinnerPriority.setSelection(task.priority)
        selectedDueDate = task.dueDate
        selectedDueDate?.let {
            val date = Date(it)
            val calendar = Calendar.getInstance().apply { timeInMillis = it }
            selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
            selectedMinute = calendar.get(Calendar.MINUTE)
            val formattedDate = dateFormat.format(date)
            val formattedTime = timeFormat.format(date)
            binding.tvSelectedDate.text = getString(R.string.due_label, "$formattedDate at $formattedTime")
        } ?: run {
            binding.tvSelectedDate.text = getString(R.string.due_no_date)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        selectedDueDate?.let { calendar.timeInMillis = it }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                showTimePickerDialog(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute

                calendar.set(year, month, day, hourOfDay, minute, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                selectedDueDate = calendar.timeInMillis

                val formattedDate = dateFormat.format(calendar.time)
                val formattedTime = timeFormat.format(calendar.time)
                binding.tvSelectedDate.text = getString(R.string.due_label, "$formattedDate at $formattedTime")
            },
            selectedHour,
            selectedMinute,
            true
        )
        timePickerDialog.show()
    }

    private fun saveOrUpdateTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTaskTitle.error = getString(R.string.please_enter_task_title)
            return
        }

        val priority = binding.spinnerPriority.selectedItemPosition

        if (currentTask == null) {
            val newTask = Task(
                id = 0,
                title = title,
                priority = priority,
                dueDate = selectedDueDate,
                isCompleted = false
            )
            viewModel.insert(newTask, requireContext())
            Toast.makeText(requireContext(), "Task added", Toast.LENGTH_SHORT).show()
        } else {
            val updatedTask = currentTask!!.copy(
                title = title,
                priority = priority,
                dueDate = selectedDueDate
            )
            viewModel.update(updatedTask, requireContext())
            Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
