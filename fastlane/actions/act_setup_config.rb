module Fastlane
  module Actions
    class ActSetupConfigAction < Action
      def self.run(params)
        UI.header("🔧 Setting up Android project configuration files")

        # Get project root (where Fastfile is located)
        project_root = params[:project_root] || Dir.pwd
        config_dir = File.join(project_root, "config")

        unless Dir.exist?(config_dir)
          UI.error("❌ Config directory not found: #{config_dir}")
          UI.error("Make sure you're running this from the project root directory.")
          UI.user_error!("Config directory not found")
        end

        UI.message("📁 Project root: #{project_root}")

        # Setup environment configuration files
        UI.message("📝 Generating environment configuration files...")
        setup_environment_configs(config_dir)

        # Setup signing configuration files
        UI.message("🔐 Generating signing configuration files...")
        setup_signing_configs(config_dir)

        # Print summary
        UI.success("🎉 Configuration setup complete!")
        UI.message("")
        UI.message("📚 Next steps:")
        UI.message("1. Update environment files with your specific API URLs and configurations")
        UI.message("2. Update release.properties with actual release signing credentials")
        UI.message("3. Ensure all sensitive values are added to .gitignore")
        UI.message("4. Run 'bundle exec fastlane build_debug' to verify the setup")
        UI.message("")
        UI.success("✅ Your project is ready for development!")

        # Return summary for other actions to use
        {
          environment_files_created: count_environment_files(config_dir),
          signing_files_created: count_signing_files(config_dir)
        }
      end

      def self.setup_environment_configs(config_dir)
        env_dir = File.join(config_dir, "environment")
        FileUtils.mkdir_p(env_dir)

        env_files = %w[dev.properties qa.properties staging.properties prod.properties]

        env_files.each do |env_file|
          env_path = File.join(env_dir, env_file)

          if File.exist?(env_path)
            UI.important("⚠️  Environment file already exists: #{env_file}")
            next
          end

          env_name = env_file.gsub('.properties', '')
          template_content = <<~EOF
            # Environment configuration for #{env_name}
            # Add your environment-specific properties here
            #
            # Example properties:
            # BASE_URL=https://api.#{env_name}.yourapp.com
            # API_KEY=your_api_key_here
            # ENABLE_LOGGING=true
            # DEBUG_MODE=false

          EOF

          File.write(env_path, template_content)
          UI.success("✅ Created environment file: #{env_file}")
        end
      end

      def self.setup_signing_configs(config_dir)
        signing_dir = File.join(config_dir, "signing")
        FileUtils.mkdir_p(signing_dir)

        debug_properties = File.join(signing_dir, "debug.properties")
        internal_properties = File.join(signing_dir, "internal.properties")
        release_properties = File.join(signing_dir, "release.properties")

        # Check if debug.properties exists (should be committed to git)
        unless File.exist?(debug_properties)
          UI.error("❌ Debug properties file not found: #{debug_properties}")
          UI.error("This file should exist in the repository. Please check your git checkout.")
          UI.user_error!("Debug properties file not found")
        end

        # Generate internal.properties (copy of debug)
        if File.exist?(internal_properties)
          UI.important("⚠️  Internal signing config already exists: internal.properties")
        else
          FileUtils.cp(debug_properties, internal_properties)
          UI.success("✅ Created internal.properties (copied from debug.properties)")
        end

        # Generate release.properties (copy of debug as template)
        if File.exist?(release_properties)
          UI.important("⚠️  Release signing config already exists: release.properties")
        else
          debug_content = File.read(debug_properties)

          release_content = <<~EOF
            # WARNING: This is a template based on debug.properties
            # Please update with actual release signing configuration
            # DO NOT commit this file with real credentials!
            #
            #{debug_content}
          EOF

          File.write(release_properties, release_content)
          UI.success("✅ Created release.properties (template - PLEASE UPDATE WITH REAL CREDENTIALS)")
          UI.important("⚠️  Remember to update release.properties with actual signing credentials!")
        end
      end

      def self.count_environment_files(config_dir)
        env_dir = File.join(config_dir, "environment")
        return 0 unless Dir.exist?(env_dir)

        Dir.glob(File.join(env_dir, "*.properties")).count
      end

      def self.count_signing_files(config_dir)
        signing_dir = File.join(config_dir, "signing")
        return 0 unless Dir.exist?(signing_dir)

        signing_files = %w[debug.properties internal.properties release.properties]
        signing_files.count { |file| File.exist?(File.join(signing_dir, file)) }
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Setup Android development configuration files (environment and signing configs)"
      end

      def self.details
        "This action generates missing configuration files that are gitignored but needed for development. " \
        "It creates environment property files (dev, qa, staging, prod) as empty templates and " \
        "signing configuration files (internal and release) based on the debug configuration."
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :project_root,
            env_name: "SETUP_CONFIG_PROJECT_ROOT",
            description: "Path to the project root directory (where config/ folder is located)",
            optional: true,
            type: String,
            default_value: Dir.pwd
          )
        ]
      end

      def self.return_value
        "Hash containing counts of created files: { environment_files_created: Int, signing_files_created: Int }"
      end

      def self.authors
        ["NewApp Template"]
      end

      def self.is_supported?(platform)
        true
      end

      def self.example_code
        [
          'setup_config',
          'setup_config(project_root: "/path/to/project")'
        ]
      end

      def self.category
        :project
      end
    end
  end
end