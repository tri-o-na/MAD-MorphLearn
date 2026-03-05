package mad.team9.morphlearn.notes

interface MaterialsDataSource {
    suspend fun getAllMaterials(): List<Material>
}