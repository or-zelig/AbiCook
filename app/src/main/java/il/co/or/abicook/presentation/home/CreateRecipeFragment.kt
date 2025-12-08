package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import il.co.or.abicook.R

// מייצג מצרך אחד לשימוש בצ'יפים
data class IngredientUi(
    val name: String,
    val amount: String,
    val unit: String
)

// תוצאת ולידציה של המצרכים
data class IngredientsValidation(
    val summary: String,
    val ingredients: List<IngredientUi>,
    val hasValid: Boolean,
    val hasPartialInvalid: Boolean
)

class CreateRecipeFragment : Fragment() {

    private val viewModel: CreateRecipeViewModel by viewModels()

    // רשימת המצרכים לשימוש במסך השלבים (צ'יפים)
    private val ingredientsForSteps = mutableListOf<IngredientUi>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_recipe, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // groups
        val groupBasic = view.findViewById<LinearLayout>(R.id.groupStepBasic)
        val groupIngredients = view.findViewById<LinearLayout>(R.id.groupStepIngredients)
        val groupSteps = view.findViewById<LinearLayout>(R.id.groupStepSteps)
        val groupSummary = view.findViewById<LinearLayout>(R.id.groupStepSummary)

        // basic fields
        val etTitle = view.findViewById<TextInputEditText>(R.id.etTitle)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etDescription)

        // containers
        val containerIngredients = view.findViewById<LinearLayout>(R.id.containerIngredients)
        val containerSteps = view.findViewById<LinearLayout>(R.id.containerSteps)

        // buttons
        val btnNextFromBasic = view.findViewById<MaterialButton>(R.id.btnNextFromBasic)
        val btnBackFromIngredients = view.findViewById<MaterialButton>(R.id.btnBackFromIngredients)
        val btnNextFromIngredients = view.findViewById<MaterialButton>(R.id.btnNextFromIngredients)
        val btnBackFromSteps = view.findViewById<MaterialButton>(R.id.btnBackFromSteps)
        val btnNextFromSteps = view.findViewById<MaterialButton>(R.id.btnNextFromSteps)
        val btnBackFromSummary = view.findViewById<MaterialButton>(R.id.btnBackFromSummary)
        val btnPublish = view.findViewById<MaterialButton>(R.id.btnPublish)
        val btnAddIngredient = view.findViewById<MaterialButton>(R.id.btnAddIngredient)
        val btnAddStep = view.findViewById<MaterialButton>(R.id.btnAddStep)

        // summary text views
        val tvSummaryTitle = view.findViewById<TextView>(R.id.tvSummaryTitle)
        val tvSummaryDescription = view.findViewById<TextView>(R.id.tvSummaryDescription)
        val tvSummaryIngredients = view.findViewById<TextView>(R.id.tvSummaryIngredients)
        val tvSummarySteps = view.findViewById<TextView>(R.id.tvSummarySteps)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // קובייה התחלתית למצרים
        addIngredientCard(containerIngredients)
        // קוביות steps ניצור רק כשנגיע לשלב steps (כדי שיהיו לנו מצרכים לצ'יפים)

        // observe state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            progressBar.isVisible = state.isLoading
            btnPublish.isEnabled = !state.isLoading

            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }

            groupBasic.isVisible = state.step == CreateRecipeStep.BASIC
            groupIngredients.isVisible = state.step == CreateRecipeStep.INGREDIENTS
            groupSteps.isVisible = state.step == CreateRecipeStep.STEPS
            groupSummary.isVisible = state.step == CreateRecipeStep.SUMMARY

            // כשעברנו לשלב steps – אם אין עדיין קוביה, ניצור אחת עם צ'יפים
            if (state.step == CreateRecipeStep.STEPS && containerSteps.childCount == 0) {
                addStepCard(containerSteps)
            }

            if (state.step == CreateRecipeStep.SUMMARY) {
                val title = etTitle.text?.toString().orEmpty()
                val desc = etDescription.text?.toString().orEmpty()

                val validation = validateIngredients(containerIngredients)
                val ingredientsSummary = validation.summary
                val stepsSummary = collectStepsSummary(containerSteps)

                tvSummaryTitle.text = "Title: $title"
                tvSummaryDescription.text = "Description: $desc"
                tvSummaryIngredients.text = "Ingredients:\n$ingredientsSummary"
                tvSummarySteps.text = "Steps:\n$stepsSummary"
            }

            if (state.success) {
                Toast.makeText(
                    requireContext(),
                    "Recipe created successfully! 🎉",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
                viewModel.resetSuccess()
            }
        }

        // כפתורי הוספה
        btnAddIngredient.setOnClickListener {
            if (!isLastIngredientValid(containerIngredients)) return@setOnClickListener
            addIngredientCard(containerIngredients)
        }

        btnAddStep.setOnClickListener {
            if (!isLastStepValid(containerSteps)) return@setOnClickListener
            addStepCard(containerSteps)
        }

        // שלב 1 -> 2
        btnNextFromBasic.setOnClickListener {
            val title = etTitle.text?.toString().orEmpty()
            val desc = etDescription.text?.toString().orEmpty()

            if (title.isBlank() || desc.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Title and description are required",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.goToIngredients()
        }

        // שלב 2 -> 1
        btnBackFromIngredients.setOnClickListener {
            viewModel.previous()
        }

        // שלב 2 -> 3 (ולידציה מלאה + העברת מצרכים לצ'יפים)
        btnNextFromIngredients.setOnClickListener {
            val validation = validateIngredients(containerIngredients)

            when {
                validation.hasPartialInvalid -> {
                    Toast.makeText(
                        requireContext(),
                        "Each ingredient must have name and amount",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !validation.hasValid -> {
                    Toast.makeText(
                        requireContext(),
                        "Please add at least one ingredient",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // שומרים את רשימת המצרכים לשימוש במסך השלבים
                    ingredientsForSteps.clear()
                    ingredientsForSteps.addAll(validation.ingredients)
                    viewModel.goToSteps()
                }
            }
        }

        // שלב 3 -> 2
        btnBackFromSteps.setOnClickListener {
            viewModel.previous()
        }

        // שלב 3 -> 4
        btnNextFromSteps.setOnClickListener {
            val hasAtLeastOne = hasAtLeastOneStep(containerSteps)

            if (!hasAtLeastOne) {
                Toast.makeText(
                    requireContext(),
                    "Please add at least one step",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.goToSummary()
        }

        // Summary -> back
        btnBackFromSummary.setOnClickListener {
            viewModel.previous()
        }

        // Publish – ולידציה מלאה שוב
        btnPublish.setOnClickListener {
            val title = etTitle.text?.toString().orEmpty()
            val desc = etDescription.text?.toString().orEmpty()

            val validation = validateIngredients(containerIngredients)
            val ingredientsSummary = validation.summary
            val stepsSummary = collectStepsSummary(containerSteps)
            val hasSteps = hasAtLeastOneStep(containerSteps)

            when {
                title.isBlank() || desc.isBlank() -> {
                    Toast.makeText(
                        requireContext(),
                        "Title and description are required",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                validation.hasPartialInvalid || !validation.hasValid -> {
                    Toast.makeText(
                        requireContext(),
                        "Please fix ingredients before publishing",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !hasSteps -> {
                    Toast.makeText(
                        requireContext(),
                        "Please add at least one step",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    viewModel.publishRecipe(title, desc, ingredientsSummary, stepsSummary)
                }
            }
        }
    }

    /* ---------- מצרכים ---------- */

    private fun addIngredientCard(container: LinearLayout) {
        val card = layoutInflater.inflate(
            R.layout.item_ingredient_input,
            container,
            false
        )

        val btnRemove = card.findViewById<MaterialButton>(R.id.btnRemoveIngredient)
        btnRemove.setOnClickListener {
            if (container.childCount <= 1) {
                Toast.makeText(
                    requireContext(),
                    "At least one ingredient is required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                container.removeView(card)
            }
        }

        container.addView(card)
    }

    private fun isLastIngredientValid(container: LinearLayout): Boolean {
        if (container.childCount == 0) return true

        val last = container.getChildAt(container.childCount - 1)
        val nameEt = last.findViewById<TextInputEditText>(R.id.etIngredientName)
        val amountEt = last.findViewById<TextInputEditText>(R.id.etIngredientAmount)

        val name = nameEt.text?.toString()?.trim().orEmpty()
        val amount = amountEt.text?.toString()?.trim().orEmpty()

        return when {
            name.isBlank() && amount.isBlank() -> {
                Toast.makeText(
                    requireContext(),
                    "Fill the current ingredient before adding a new one",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            name.isBlank() || amount.isBlank() -> {
                Toast.makeText(
                    requireContext(),
                    "Ingredient must have both name and amount",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            else -> true
        }
    }

    // בונה רשימת מצרכים + summary + פלגים לולידציה
    private fun validateIngredients(container: LinearLayout): IngredientsValidation {
        val list = mutableListOf<String>()
        val ingredientObjects = mutableListOf<IngredientUi>()
        var hasValid = false
        var hasPartialInvalid = false

        for (i in 0 until container.childCount) {
            val item = container.getChildAt(i)
            val nameEt = item.findViewById<TextInputEditText>(R.id.etIngredientName)
            val amountEt = item.findViewById<TextInputEditText>(R.id.etIngredientAmount)
            val unitSpinner = item.findViewById<Spinner>(R.id.spinnerUnit)

            val name = nameEt.text?.toString()?.trim().orEmpty()
            val amount = amountEt.text?.toString()?.trim().orEmpty()
            val unit = unitSpinner.selectedItem?.toString() ?: ""

            if (name.isBlank() && amount.isBlank()) {
                continue
            }

            if (name.isBlank() || amount.isBlank()) {
                hasPartialInvalid = true
                continue
            }

            val unitPart = if (unit.isNotBlank()) " $unit" else ""
            list.add("$amount$unitPart - $name")
            ingredientObjects.add(IngredientUi(name, amount, unit))
            hasValid = true
        }

        val summary = list.joinToString("\n")
        return IngredientsValidation(
            summary = summary,
            ingredients = ingredientObjects,
            hasValid = hasValid,
            hasPartialInvalid = hasPartialInvalid
        )
    }

    /* ---------- שלבים ---------- */

    private fun addStepCard(container: LinearLayout) {
        val card = layoutInflater.inflate(
            R.layout.item_step_input,
            container,
            false
        )

        val etStep = card.findViewById<TextInputEditText>(R.id.etStepDescription)
        val chipGroup = card.findViewById<ChipGroup>(R.id.chipGroupIngredients)
        val btnRemove = card.findViewById<MaterialButton>(R.id.btnRemoveStep)

        // יצירת צ'יפים לכל מצרך שהגדרנו בשלב השני
        chipGroup.removeAllViews()
        for (ingredient in ingredientsForSteps) {
            val chipText = buildIngredientDisplay(ingredient)
            val chip = Chip(requireContext()).apply {
                text = chipText
                isCheckable = false
                isClickable = true
            }

            chip.setOnClickListener {
                insertTextAtCursor(etStep, chipText)
            }

            chipGroup.addView(chip)
        }

        btnRemove.setOnClickListener {
            if (container.childCount <= 1) {
                Toast.makeText(
                    requireContext(),
                    "At least one step is required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                container.removeView(card)
            }
        }

        container.addView(card)
    }

    private fun buildIngredientDisplay(ingredient: IngredientUi): String {
        val unitPart = if (ingredient.unit.isNotBlank()) " ${ingredient.unit}" else ""
        return "${ingredient.amount}$unitPart ${ingredient.name}"
    }

    private fun insertTextAtCursor(et: TextInputEditText, textToInsert: String) {
        val oldText = et.text?.toString().orEmpty()
        val cursorPos = et.selectionStart.coerceAtLeast(0)

        val newText = buildString {
            append(oldText.substring(0, cursorPos))
            if (cursorPos > 0 && !oldText[cursorPos - 1].isWhitespace()) {
                append(" ")
            }
            append(textToInsert)
            append(" ")
            if (cursorPos < oldText.length && !oldText[cursorPos].isWhitespace()) {
                append(oldText.substring(cursorPos))
            } else if (cursorPos < oldText.length) {
                append(oldText.substring(cursorPos))
            }
        }

        et.setText(newText)
        et.setSelection(newText.length.coerceAtLeast(0))
    }

    private fun isLastStepValid(container: LinearLayout): Boolean {
        if (container.childCount == 0) return true

        val last = container.getChildAt(container.childCount - 1)
        val etStep = last.findViewById<TextInputEditText>(R.id.etStepDescription)
        val text = etStep.text?.toString()?.trim().orEmpty()

        return if (text.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Fill the current step before adding a new one",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            true
        }
    }

    private fun hasAtLeastOneStep(container: LinearLayout): Boolean {
        for (i in 0 until container.childCount) {
            val item = container.getChildAt(i)
            val etStep = item.findViewById<TextInputEditText>(R.id.etStepDescription)
            val text = etStep.text?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) return true
        }
        return false
    }

    private fun collectStepsSummary(container: LinearLayout): String {
        val list = mutableListOf<String>()

        for (i in 0 until container.childCount) {
            val item = container.getChildAt(i)
            val etStep = item.findViewById<TextInputEditText>(R.id.etStepDescription)
            val text = etStep.text?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) {
                list.add("${i + 1}. $text")
            }
        }

        return list.joinToString(separator = "\n")
    }
}
