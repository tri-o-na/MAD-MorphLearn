package mad.team9.morphlearn.notes

interface MaterialsDataSource {
    suspend fun getAllMaterials(): List<Material>

    suspend fun checkQuizAttempt(quizId: String?): Boolean
    suspend fun getLatestQuiz(materialId: String): String?

}