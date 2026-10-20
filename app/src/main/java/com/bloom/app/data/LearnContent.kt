package com.bloom.app.data

data class FaqItem(val question: String, val answer: String)

data class FaqSection(val title: String, val items: List<FaqItem>)
object LearnContent {

    val sections: List<FaqSection> = listOf(
        FaqSection(
            "Diagnosis & basics",
            listOf(
                FaqItem(
                    "What exactly is PCOS?",
                    "PCOS stands for polycystic ovary syndrome, a common hormonal condition that affects how the ovaries work. It can cause irregular periods, higher levels of androgens (sometimes called \"male\" hormones), and small follicles on the ovaries.",
                ),
                FaqItem(
                    "What does PCOS stand for — and do I actually have cysts on my ovaries?",
                    "It stands for polycystic ovary syndrome. Despite the name, the \"cysts\" are usually small, immature follicles rather than true cysts, and you can have PCOS without them showing on an ultrasound.",
                ),
                FaqItem(
                    "What causes PCOS?",
                    "The exact cause isn't known. Genetics, insulin resistance and higher androgen levels are all thought to play a part.",
                ),
                FaqItem(
                    "How is PCOS actually diagnosed?",
                    "Doctors usually look for at least two of three features: irregular or absent ovulation, signs of high androgens (from symptoms or blood tests), and polycystic-appearing ovaries on ultrasound, after ruling out other causes.",
                ),
                FaqItem(
                    "Is PCOS the same for everyone?",
                    "No. Symptoms and severity vary a lot from person to person and can change over time.",
                ),
            ),
        ),
        FaqSection(
            "Symptoms & body",
            listOf(
                FaqItem(
                    "Why are my periods irregular?",
                    "PCOS can disrupt regular ovulation, so cycles may be long, unpredictable or occasionally missed. Tracking your dates can help you and your doctor see your pattern.",
                ),
                FaqItem(
                    "Why does weight feel harder to manage?",
                    "Insulin resistance, which is common with PCOS, can make hunger harder to regulate and the body more likely to store energy. Steady meals, movement and sleep can help, and a clinician can give personal advice.",
                ),
                FaqItem(
                    "What is the deal with insulin resistance?",
                    "With insulin resistance the body's cells respond less well to insulin, so the body makes more of it. Higher insulin can raise androgens, which affects periods, skin and hair.",
                ),
                FaqItem(
                    "Is the acne and hair growth from PCOS treatable?",
                    "Yes. There are several options, from skincare to medication, depending on your goals. A doctor or dermatologist can help you choose what suits you.",
                ),
            ),
        ),
        FaqSection(
            "Managing it day to day",
            listOf(
                FaqItem(
                    "Can food really make a difference?",
                    "Balanced meals with fibre, protein and healthy fats can help steady blood sugar, which many people with PCOS find helps their energy and symptoms.",
                ),
                FaqItem(
                    "What foods should I eat — and which should I limit?",
                    "Whole foods such as vegetables, legumes, whole grains, lean proteins and healthy fats are a good base. Highly processed, sugary foods can spike blood sugar, so many people limit them. There is no single PCOS diet.",
                ),
                FaqItem(
                    "How can I manage insulin resistance day to day?",
                    "Regular movement, balanced meals, good sleep and stress management all support insulin sensitivity. Some people also take medication prescribed by their doctor.",
                ),
                FaqItem(
                    "Will I need medication forever?",
                    "Not necessarily. Treatment depends on your symptoms and goals and can change over time, so review it with your doctor regularly.",
                ),
                FaqItem(
                    "How does stress fit into this?",
                    "Ongoing stress raises cortisol, which can affect blood sugar, sleep and mood. Short daily relaxation practices can help.",
                ),
            ),
        ),
        FaqSection(
            "Looking ahead",
            listOf(
                FaqItem(
                    "Does PCOS affect fertility?",
                    "PCOS can make ovulation less regular, which can make conceiving take longer. Many people with PCOS do conceive, sometimes with medical support.",
                ),
                FaqItem(
                    "What happens if PCOS is left unmanaged?",
                    "Over time it can raise the risk of issues such as type 2 diabetes, high blood pressure and heart disease, which is why regular check-ups matter.",
                ),
            ),
        ),
    )

    val allFaqs: List<FaqItem> = sections.flatMap { it.items }
}
