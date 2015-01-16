module.exports = function(grunt) {
	
	var 
		sourceFiles = [
			'src/index.coffee',
			'src/utils/*.coffee',
			'src/model/*.coffee',
			//'src/models/index.coffee',
			'src/views/*.coffee'
		],
		coffeeOptions = {
			sourceMap: true,
			bare: false,
			join: true
			
		}
		
	grunt.initConfig({
		coffee: {
			default: {
				options: coffeeOptions,
				files:{
					'dist/fingerboard.js': sourceFiles
				}
			},
			play: {
				options: coffeeOptions,
				files: {
					'../public/javascripts/fingerboard.js': sourceFiles
				}
			}
		},
		/*concat: {
			default: {
				files: {
					'dist/fingerboard.js': files
				},
				options: concatOptions
			},
			play: {
				files: {
					'../public/javascripts/fingerboard.js': files
				},
				options: concatOptions
			}
		},*/
		uglify: {
			default: {
				files: {
					'dist/fingerboard.min.js': ['dist/fingerboard.js']
				}
			},
			play: {
				files: {
					'../public/javascripts/fingerboard.min.js': ['../public/javascripts/fingerboard.js']
				}
			}
		},
		watch:{
			default:{
				files: sourceFiles,
				tasks: ['coffee:default'],
				options: {
					atBegin: true,
					spawn: false
				}
			},
			play:{
				files: sourceFiles,
				tasks: ['coffee:play'],
				options: {
					atBegin: true
				}
			}
			
		}
	});
	
	
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-coffee');
	
	grunt.registerTask('default', ['coffee:default', 'uglify:default']);
	grunt.registerTask('play', ['concat:play', 'uglify:play']);
	
};