module.exports = ->

	@initConfig(
		coffee:
			default:
				options:
					sourceMap: true
					bare: false
					join: true
				expand: true
				flatten: false
				cwd: 'path/to'
				src: ['app/assets/javascripts/*.coffee']
				dest: 'public/javascripts'
				ext: '.js'
		watch:
			default:
				files: ['app/assets/javascripts/*.coffee']
				tasks: ['coffee:default']
				options:
					atBegin: true
					spawn: false

	)

	@loadNpmTasks('grunt-contrib-watch')
	@loadNpmTasks('grunt-contrib-coffee')

	
