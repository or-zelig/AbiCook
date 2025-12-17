package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import il.co.or.abicook.R
import android.widget.ArrayAdapter

class CreateRecipeFragment : Fragment() {

    private lateinit var viewModel: CreateRecipeViewModel

    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etPrepTime: TextInputEditText
    private lateinit var etCookTime: TextInputEditText
    private lateinit var chipGroupCategories: ChipGroup

    private lateinit var containerIngredients: LinearLayout
    private lateinit var btnAddIngredient: MaterialButton

    private lateinit var containerSteps: LinearLayout
    private lateinit var btnAddStep: MaterialButton

    private lateinit var tvError: TextView
    private lateinit var progressBar: View
    private lateinit var btnPublish: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_recipe, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar – חץ אחורה
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBarCreateRecipe)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProvider(this)[CreateRecipeViewModel::class.java]

        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        etPrepTime = view.findViewById(R.id.etPrepTime)
        etCookTime = view.findViewById(R.id.etCookTime)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)

        containerIngredients = view.findViewById(R.id.containerIngredients)
        btnAddIngredient = view.findViewById(R.id.btnAddIngredient)

        containerSteps = view.findViewById(R.id.containerSteps)
        btnAddStep = view.findViewById(R.id.btnAddStep)

        tvError = view.findViewById(R.id.tvError)
        progressBar = view.findViewById(R.id.progressBar)
        btnPublish = view.findViewById(R.id.btnPublishRecipe)

        setupCategoriesChips()

        // מתחילים עם מצרך ושלב אחד
        addIngredientView()
        addStepView()
        refreshStepIngredientChips()

        btnAddIngredient.setOnClickListener {
            if (validateLastIngredientFilled()) {
                addIngredientView()
                refreshStepIngredientChips()
            } else {
                showToast("Fill the current ingredient before adding a new one")
            }
        }

        btnAddStep.setOnClickListener {
            if (validateLastStepFilled()) {
                addStepView()
                refreshStepIngredientChips()
            } else {
                showToast("Fill the current step before adding a new one")
            }
        }

        btnPublish.setOnClickListener {
            publishRecipe()
        }

        observeViewModel()
    }

    // region UI helpers

    /** בונה צ'יפים של קטגוריות מתוך R.array.recipe_categories */
    private fun setupCategoriesChips() {
        chipGroupCategories.removeAllViews()
        val categories = resources.getStringArray(R.array.recipe_categories)
        categories.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isClickable = true
            }
            chipGroupCategories.addView(chip)
        }
    }

    /** הוספת כרטיס מצרך חדש + חיבור ה-dropdown של UNIT + text watcher לעדכון הצ'יפים בשלבים */
    private fun addIngredientView() {
        val itemView = layoutInflater.inflate(
            R.layout.item_create_ingredient,
            containerIngredients,
            false
        )

        val etName = itemView.findViewById<TextInputEditText>(R.id.etIngredientName)
        val etAmount = itemView.findViewById<TextInputEditText>(R.id.etIngredientAmount)
        val etUnit = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.etIngredientUnit)

        // DROPDOWN אמיתי ל-UNIT
        val units = resources.getStringArray(R.array.ingredient_units)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units.toList())
        etUnit.setAdapter(adapter)
        etUnit.setOnClickListener { etUnit.showDropDown() }

        // כשמשנים שם מצרך – נעדכן את הצ'יפים בשלבים
        etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                refreshStepIngredientChips()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val btnRemove = itemView.findViewById<MaterialButton>(R.id.btnRemoveIngredient)
        btnRemove.setOnClickListener {
            if (containerIngredients.childCount > 1) {
                containerIngredients.removeView(itemView)
                refreshStepIngredientChips()
            } else {
                showToast("You must have at least one ingredient")
            }
        }

        containerIngredients.addView(itemView)
    }

    /** הוספת כרטיס שלב כולל ChipGroup למצרכים */
    private fun addStepView() {
        val itemView = layoutInflater.inflate(
            R.layout.item_create_step,
            containerSteps,
            false
        )

        val btnRemove = itemView.findViewById<MaterialButton>(R.id.btnRemoveStep)
        btnRemove.setOnClickListener {
            if (containerSteps.childCount > 1) {
                containerSteps.removeView(itemView)
            } else {
                showToast("You must have at least one step")
            }
        }

        containerSteps.addView(itemView)
    }

    /** מחזיר את כל שמות המצרכים (trim ולא ריקים) */
    private fun getIngredientNames(): List<String> =
        containerIngredients.children.mapNotNull { child ->
            child.findViewById<TextInputEditText>(R.id.etIngredientName)
                .text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        }.toList()

    /**
     * בונה את הצ'יפים של המצרכים לכל שלב:
     * – כל צ'יפ מייצג מצרך
     * – לחיצה על צ'יפ מוסיפה את השם לתיאור השלב (כדי שלא תצטרך לכתוב שוב)
     */
    private fun refreshStepIngredientChips() {
        val ingredientNames = getIngredientNames()

        containerSteps.children.forEach { stepView ->
            val chipGroup =
                stepView.findViewById<ChipGroup?>(R.id.chipGroupStepIngredients) ?: return@forEach

            // נשמור מי היה מסומן לפני הרענון
            val previouslyChecked = chipGroup.children
                .mapNotNull { it as? Chip }
                .filter { it.isChecked }
                .map { it.text.toString() }
                .toSet()

            chipGroup.removeAllViews()

            val etStep =
                stepView.findViewById<TextInputEditText>(R.id.etStepDescription)

            ingredientNames.forEach { name ->
                val chip = Chip(requireContext()).apply {
                    text = name
                    isCheckable = true
                    isChecked = previouslyChecked.contains(name)

                    setOnClickListener {
                        // הוספת שם המצרך לטקסט של השלב (שלא תהיה כתיבה כפולה)
                        val current = etStep.text?.toString().orEmpty()
                        val toAppend = if (current.isBlank()) {
                            name
                        } else if (current.endsWith(" ", ignoreCase = true) ||
                            current.endsWith(",", ignoreCase = true)
                        ) {
                            "$current$name"
                        } else {
                            "$current, $name"
                        }
                        etStep.setText(toAppend)
                        etStep.setSelection(etStep.text?.length ?: 0)
                    }
                }
                chipGroup.addView(chip)
            }
        }
    }

    private fun validateLastIngredientFilled(): Boolean {
        if (containerIngredients.childCount == 0) return true
        val last = containerIngredients.getChildAt(containerIngredients.childCount - 1)
        val name =
            last.findViewById<TextInputEditText>(R.id.etIngredientName).text?.toString()?.trim()
        val amount =
            last.findViewById<TextInputEditText>(R.id.etIngredientAmount).text?.toString()?.trim()
        val unit =
            last.findViewById<MaterialAutoCompleteTextView>(R.id.etIngredientUnit).text?.toString()
                ?.trim()

        return !name.isNullOrEmpty() && !amount.isNullOrEmpty() && !unit.isNullOrEmpty()
    }

    private fun validateLastStepFilled(): Boolean {
        if (containerSteps.childCount == 0) return true
        val last = containerSteps.getChildAt(containerSteps.childCount - 1)
        val desc =
            last.findViewById<TextInputEditText>(R.id.etStepDescription).text?.toString()?.trim()
        return !desc.isNullOrEmpty()
    }

    private fun validateAllFields(): Boolean {
        tvError.visibility = View.GONE
        tvError.text = ""

        val title = etTitle.text?.toString()?.trim().orEmpty()
        val desc = etDescription.text?.toString()?.trim().orEmpty()

        if (title.isEmpty()) {
            tvError.text = "Title is required"
            tvError.visibility = View.VISIBLE
            return false
        }

        if (desc.isEmpty()) {
            tvError.text = "Description is required"
            tvError.visibility = View.VISIBLE
            return false
        }

        if (containerIngredients.childCount == 0) {
            tvError.text = "Add at least one ingredient"
            tvError.visibility = View.VISIBLE
            return false
        }

        if (containerSteps.childCount == 0) {
            tvError.text = "Add at least one step"
            tvError.visibility = View.VISIBLE
            return false
        }

        // כל המצרכים מלאים
        containerIngredients.children.forEach { child ->
            val name =
                child.findViewById<TextInputEditText>(R.id.etIngredientName).text?.toString()
                    ?.trim()
            val amount =
                child.findViewById<TextInputEditText>(R.id.etIngredientAmount).text?.toString()
                    ?.trim()
            val unit =
                child.findViewById<MaterialAutoCompleteTextView>(R.id.etIngredientUnit).text
                    ?.toString()?.trim()

            if (name.isNullOrEmpty() || amount.isNullOrEmpty() || unit.isNullOrEmpty()) {
                tvError.text = "All ingredients must be fully filled"
                tvError.visibility = View.VISIBLE
                return false
            }
        }

        // כל השלבים מלאים
        containerSteps.children.forEach { child ->
            val descStep =
                child.findViewById<TextInputEditText>(R.id.etStepDescription).text?.toString()
                    ?.trim()
            if (descStep.isNullOrEmpty()) {
                tvError.text = "All steps must be fully filled"
                tvError.visibility = View.VISIBLE
                return false
            }
        }

        return true
    }

    private fun buildIngredientsSummary(): String {
        val builder = StringBuilder()
        containerIngredients.children.forEach { child ->
            val name =
                child.findViewById<TextInputEditText>(R.id.etIngredientName).text?.toString()
                    ?.trim().orEmpty()
            val amount =
                child.findViewById<TextInputEditText>(R.id.etIngredientAmount).text?.toString()
                    ?.trim().orEmpty()
            val unit =
                child.findViewById<MaterialAutoCompleteTextView>(R.id.etIngredientUnit).text
                    ?.toString()?.trim().orEmpty()

            builder.append("- ")
                .append(name)
                .append(" – ")
                .append(amount)
                .append(" ")
                .append(unit)
                .append("\n")
        }
        return builder.toString().trimEnd()
    }

    private fun buildStepsSummary(): String {
        val builder = StringBuilder()
        var index = 1
        containerSteps.children.forEach { child ->
            val desc =
                child.findViewById<TextInputEditText>(R.id.etStepDescription).text?.toString()
                    ?.trim().orEmpty()
            builder.append(index).append(". ").append(desc).append("\n")
            index++
        }
        return builder.toString().trimEnd()
    }

    private fun publishRecipe() {
        if (!validateAllFields()) return

        val title = etTitle.text?.toString()?.trim().orEmpty()
        val description = etDescription.text?.toString()?.trim().orEmpty()
        val ingredientsSummary = buildIngredientsSummary()
        val stepsSummary = buildStepsSummary()

        val ingredientNames = getIngredientNames()

        // בדיקה שכל מצרך מופיע לפחות באחד השלבים לפי ה-chips
        val usedIngredients = mutableSetOf<String>()
        containerSteps.children.forEach { stepView ->
            val chipGroup =
                stepView.findViewById<ChipGroup?>(R.id.chipGroupStepIngredients) ?: return@forEach
            chipGroup.children.forEach { chipView ->
                val chip = chipView as? Chip ?: return@forEach
                if (chip.isChecked) {
                    usedIngredients += chip.text.toString()
                }
            }
        }

        val unusedIngredients = ingredientNames.filter { it !in usedIngredients }

        if (unusedIngredients.isNotEmpty()) {
            tvError.text =
                "Some ingredients are not used in the steps: ${
                    unusedIngredients.joinToString(
                        ", "
                    )
                }"
            tvError.visibility = View.VISIBLE
            return
        }

        val prep = etPrepTime.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val cook = etCookTime.text?.toString()?.trim()?.toIntOrNull() ?: 0

        val selected = chipGroupCategories.children
            .mapNotNull { it as? Chip }
            .filter { it.isChecked }
            .map { it.text.toString() }
            .toList()

        val primaryCategory = selected.firstOrNull().orEmpty()

        viewModel.publishRecipe(
            title = title,
            description = description,
            ingredientsSummary = ingredientsSummary,
            stepsSummary = stepsSummary,
            primaryCategory = primaryCategory,
            categories = selected,
            prepTimeMin = prep,
            cookTimeMin = cook
        )
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            btnPublish.isEnabled = !state.isLoading

            if (state.error != null) {
                tvError.text = state.error
                tvError.visibility = View.VISIBLE
            } else {
                tvError.visibility = View.GONE
            }

            if (state.success) {
                showToast("Recipe published!")
                clearForm()
                findNavController().navigateUp()
                viewModel.onHandledSuccess()
            }
        }
    }

    private fun clearForm() {
        etTitle.setText("")
        etDescription.setText("")
        etPrepTime.setText("")
        etCookTime.setText("")
        chipGroupCategories.clearCheck()

        containerIngredients.removeAllViews()
        containerSteps.removeAllViews()
        addIngredientView()
        addStepView()
        refreshStepIngredientChips()
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
